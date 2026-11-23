package cl.duoc.mpazneb.data.repository
import android.util.Log

import cl.duoc.mpazneb.data.local.AsignacionDao
import cl.duoc.mpazneb.data.remote.ApiService
import cl.duoc.mpazneb.data.remote.dto.AsignacionRequest
import cl.duoc.mpazneb.data.remote.dto.ValidarCodigoRequest
import cl.duoc.mpazneb.domain.CodigosDemostracion
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.AsignacionDetalle
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.Estudiante
import cl.duoc.mpazneb.model.Observacion
import cl.duoc.mpazneb.model.Respuesta
import cl.duoc.mpazneb.model.Rol
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import retrofit2.Response

sealed interface ResultadoCodigo {
    data class Valido(val asignacion: Asignacion) : ResultadoCodigo
    data object NoExiste : ResultadoCodigo
    data object Vencido : ResultadoCodigo
    data object YaUsado : ResultadoCodigo
    data class Conflicto(val mensaje: String) : ResultadoCodigo
    data class ErrorRed(val mensaje: String) : ResultadoCodigo
    data class ReferenciasLocalesFaltantes(val mensaje: String) : ResultadoCodigo
}

fun clasificarCodigo(asignacion: Asignacion?, ahora: Long): ResultadoCodigo {
    if (asignacion == null) return ResultadoCodigo.NoExiste
    if (asignacion.estado == EstadoAsignacion.FINALIZADA) return ResultadoCodigo.YaUsado
    if (asignacion.venceEn < ahora) return ResultadoCodigo.Vencido
    return ResultadoCodigo.Valido(asignacion)
}

class AsignacionRepository(
    private val dao: AsignacionDao,
    private val auth: AuthRepository,
    private val actividades: ActividadRepository,
    private val apiService: ApiService,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    companion object {
        const val DURACION_CODIGO_MS = 30 * 60 * 1000L
    }

    fun estudiantesPorCurso(curso: Int): Flow<List<Estudiante>> = dao.estudiantesPorCurso(curso)

    suspend fun aliasExiste(alias: String): Boolean = dao.contarAlias(alias.trim()) > 0

    suspend fun crearEstudiante(alias: String, curso: Int): Long =
        dao.insertarEstudiante(Estudiante(alias = alias.trim(), curso = curso))

    suspend fun tieneSesionAbierta(estudianteId: Long): Boolean =
        dao.sesionesAbiertas(estudianteId, reloj()) > 0

    /** Pide el código a Spring Boot y guarda la copia local con ids locales. */
    suspend fun crear(estudianteId: Long, actividadId: Long): ResultadoAccion {
        val asistenteId = exigirAsistente()
            ?: return ResultadoAccion.Error("Solo la asistente de aula puede asignar actividades.")

        val actividadLocal = actividades.obtener(actividadId)
        if (actividadLocal == null || actividadLocal.estado != EstadoActividad.DISPONIBLE) {
            return ResultadoAccion.Error("Esa actividad no está disponible para nuevas asignaciones.")
        }
        if (tieneSesionAbierta(estudianteId)) {
            return ResultadoAccion.Error("Ese estudiante ya tiene una actividad abierta.")
        }

        val remoto = when (val resuelto = actividades.idRemotoDe(actividadLocal)) {
            is ResultadoAccion.Error -> return resuelto
            is ResultadoAccion.Exito -> resuelto.id
            else -> return ResultadoAccion.Error("No se pudo resolver el id remoto.")
        }

        val existente = dao.asignacionVigente(estudianteId, actividadId, reloj())
        if (existente != null) {
            return ResultadoAccion.Exito(id = existente.id, asignacion = existente)
        }

        return try {
            val respuesta = apiService.crearAsignacion(
                AsignacionRequest(
                    estudianteId = estudianteId,
                    actividadId = remoto,
                    asistenteId = asistenteId
                )
            )
            if (!respuesta.isSuccessful || respuesta.body() == null) {
                return ResultadoAccion.Error(mensajeHttp(respuesta, "No se pudo crear la asignación en el servidor."))
            }
            val dto = respuesta.body()!!
            val local = Asignacion(
                estudianteId = estudianteId,
                actividadId = actividadId,
                asistenteId = asistenteId,
                codigo = dto.codigo,
                creadaEn = dto.creadaEn,
                venceEn = dto.venceEn,
                estado = EstadoAsignacion.valueOf(dto.estado)
            )
            val id = dao.insertar(local)
            ResultadoAccion.Exito(id = id, asignacion = local.copy(id = id))

        } catch (e: IOException) {
            Log.e("MPAZ_API", "Error al crear asignación", e)

            ResultadoAccion.Error(
                "No hay conexión con el servidor. Comprueba que Spring Boot esté encendido y reintenta."
            )
        }
        catch (_: HttpException) {
            ResultadoAccion.Error("El servidor rechazó la asignación.")
        }
    }

    suspend fun regenerarCodigo(asignacionId: Long): ResultadoAccion {
        val asistenteId = exigirAsistente()
            ?: return ResultadoAccion.Error("Solo la asistente de aula puede renovar un código.")
        val actual = dao.obtener(asignacionId)
            ?: return ResultadoAccion.Error("No se encontró la asignación.")
        if (actual.asistenteId != asistenteId) {
            return ResultadoAccion.Error("Solo puedes renovar tus propias asignaciones.")
        }
        if (actual.estado == EstadoAsignacion.FINALIZADA) {
            return ResultadoAccion.Error("La actividad finalizada conserva su historial. No se cambia el código.")
        }
        return ResultadoAccion.Error("Renueva el código desde una nueva asignación con el servidor.")
    }

    suspend fun validarCodigo(codigo: String): ResultadoCodigo {
        dao.buscarPorCodigo(codigo)?.let { return clasificarCodigo(it, reloj()) }
        if (CodigosDemostracion.esDeSemilla(codigo)) {
            return clasificarCodigo(null, reloj())
        }

        return try {
            val respuesta = apiService.validarCodigo(ValidarCodigoRequest(codigo))
            when {
                respuesta.isSuccessful && respuesta.body() != null -> {
                    val dto = respuesta.body()!!
                    dao.buscarPorCodigo(codigo)?.let { return clasificarCodigo(it, reloj()) }

                    val actividadLocal = when (val resolucion = actividades.actividadLocalParaIdRemoto(dto.actividadId)) {
                        is ResolucionActividadLocal.Lista -> resolucion.actividad
                        ResolucionActividadLocal.SinCoincidenciaLocal ->
                            return ResultadoCodigo.ReferenciasLocalesFaltantes(
                                "El código es válido, pero esta tablet no tiene la actividad enlazada. " +
                                    "Abre la app con internet para sincronizar el catálogo o pide ayuda a tu asistente."
                            )
                        ResolucionActividadLocal.Ambigua ->
                            return ResultadoCodigo.ReferenciasLocalesFaltantes(
                                "Hay más de una actividad local parecida. Pide ayuda a tu asistente."
                            )
                        ResolucionActividadLocal.SinActividadEnCatalogo ->
                            return ResultadoCodigo.ReferenciasLocalesFaltantes(
                                "El servidor no devolvió la actividad del código."
                            )
                        ResolucionActividadLocal.ErrorRed ->
                            return ResultadoCodigo.ErrorRed(
                                "No hay conexión con el servidor. Comprueba la red y vuelve a intentar."
                            )
                        ResolucionActividadLocal.ErrorRemoto ->
                            return ResultadoCodigo.ErrorRed(
                                "No se pudo consultar el catálogo del servidor."
                            )
                    }

                    if (dao.obtenerEstudiante(dto.estudianteId) == null) {
                        return ResultadoCodigo.ReferenciasLocalesFaltantes(
                            "El código es válido, pero no encontramos al estudiante en esta tablet."
                        )
                    }
                    if (dao.existeAsistente(dto.asistenteId) == 0) {
                        return ResultadoCodigo.ReferenciasLocalesFaltantes(
                            "El código es válido, pero falta la asistente en los datos locales."
                        )
                    }

                    val nueva = asignacionLocalDesdeRemota(dto, actividadLocal.id)
                    val id = dao.insertar(nueva)
                    clasificarCodigo(nueva.copy(id = id), reloj())
                }
                respuesta.code() == 404 -> ResultadoCodigo.NoExiste
                respuesta.code() == 410 -> ResultadoCodigo.Vencido
                respuesta.code() == 409 -> ResultadoCodigo.Conflicto("Ese código ya se usó.")
                else -> ResultadoCodigo.NoExiste
            }
        } catch (_: IOException) {
            ResultadoCodigo.ErrorRed(
                "No hay conexión con el servidor. Comprueba la red y vuelve a intentar."
            )
        } catch (_: HttpException) {
            ResultadoCodigo.ErrorRed("No se pudo validar el código en el servidor.")
        }
    }

    suspend fun obtener(id: Long): Asignacion? = dao.obtener(id)

    suspend fun iniciar(id: Long) = dao.marcarEnDesarrollo(id, reloj())

    suspend fun finalizar(id: Long) = dao.finalizar(id, reloj())

    fun delAsistente(asistenteId: Long): Flow<List<AsignacionDetalle>> = dao.delAsistente(asistenteId)

    fun detalle(id: Long): Flow<AsignacionDetalle?> = dao.detalle(id)

    suspend fun registrarRespuesta(
        asignacionId: Long,
        preguntaId: Long,
        alternativaId: Long,
        correcta: Boolean,
        intento: Int
    ) {
        dao.insertarRespuesta(
            Respuesta(
                asignacionId = asignacionId,
                preguntaId = preguntaId,
                alternativaId = alternativaId,
                correcta = correcta,
                intento = intento,
                fecha = reloj()
            )
        )
    }

    fun respuestas(asignacionId: Long): Flow<List<Respuesta>> = dao.respuestas(asignacionId)

    suspend fun guardarObservacion(asignacionId: Long, categoria: String, texto: String): ResultadoAccion {
        if (exigirAsistente() == null) {
            return ResultadoAccion.Error("Solo la asistente de aula puede registrar observaciones.")
        }
        if (dao.obtener(asignacionId) == null) {
            return ResultadoAccion.Error("No se encontró la asignación.")
        }
        dao.insertarObservacion(
            Observacion(
                asignacionId = asignacionId,
                categoria = categoria,
                texto = texto.trim(),
                fecha = reloj()
            )
        )
        return ResultadoAccion.Exito(id = asignacionId)
    }

    fun observaciones(asignacionId: Long): Flow<List<Observacion>> = dao.observaciones(asignacionId)

    private suspend fun exigirAsistente(): Long? {
        val sesion = auth.sesionActual.first() ?: return null
        if (sesion.rol != Rol.ASISTENTE) return null
        return sesion.id
    }

    private fun mensajeHttp(respuesta: Response<*>, defecto: String): String {
        return when (respuesta.code()) {
            400 -> "Datos inválidos para la asignación."
            404 -> "La actividad no existe en el servidor."
            409 -> "Ese estudiante ya tiene una actividad abierta."
            else -> defecto
        }
    }
}

internal fun interpretarValidacionHttp(codigoHttp: Int): ResultadoCodigo? = when (codigoHttp) {
    404 -> ResultadoCodigo.NoExiste
    410 -> ResultadoCodigo.Vencido
    409 -> ResultadoCodigo.Conflicto("Ese código ya se usó.")
    else -> null
}

internal fun asignacionLocalDesdeRemota(dto: cl.duoc.mpazneb.data.remote.dto.AsignacionDto, actividadLocalId: Long) =
    Asignacion(
        estudianteId = dto.estudianteId,
        actividadId = actividadLocalId,
        asistenteId = dto.asistenteId,
        codigo = dto.codigo,
        creadaEn = dto.creadaEn,
        venceEn = dto.venceEn,
        estado = EstadoAsignacion.valueOf(dto.estado)
    )