package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.ActividadDao
import cl.duoc.mpazneb.data.local.AsignacionDao
import cl.duoc.mpazneb.domain.validation.CodigoValidator
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.AsignacionDetalle
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.Estudiante
import cl.duoc.mpazneb.model.Observacion
import cl.duoc.mpazneb.model.Respuesta
import cl.duoc.mpazneb.model.Rol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.random.Random

sealed interface ResultadoCodigo {
    data class Valido(val asignacion: Asignacion) : ResultadoCodigo
    data object NoExiste : ResultadoCodigo
    data object Vencido : ResultadoCodigo
    data object YaUsado : ResultadoCodigo
}

/**
 * Decide si un código deja entrar.
 * Primero mira si existe, después si la actividad ya terminó, después si venció.
 * Una asignación en desarrollo y vigente se puede retomar.
 */
fun clasificarCodigo(asignacion: Asignacion?, ahora: Long): ResultadoCodigo {
    if (asignacion == null) return ResultadoCodigo.NoExiste
    if (asignacion.estado == EstadoAsignacion.FINALIZADA) return ResultadoCodigo.YaUsado
    if (asignacion.venceEn < ahora) return ResultadoCodigo.Vencido
    return ResultadoCodigo.Valido(asignacion)
}

class AsignacionRepository(
    private val dao: AsignacionDao,
    private val actividades: ActividadDao,
    private val auth: AuthRepository,
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

    /**
     * Crea la asignación con un código de seis dígitos que no esté en uso.
     * El código vence a los 30 minutos. La asistente no puede asignar una actividad desactivada.
     */
    suspend fun crear(estudianteId: Long, actividadId: Long): ResultadoAccion {
        val asistenteId = exigirAsistente()
            ?: return ResultadoAccion.Error("Solo la asistente de aula puede asignar actividades.")
        val actividad = actividades.obtener(actividadId)
        if (actividad == null || actividad.estado != EstadoActividad.DISPONIBLE) {
            return ResultadoAccion.Error("Esa actividad no está disponible para nuevas asignaciones.")
        }
        if (tieneSesionAbierta(estudianteId)) {
            return ResultadoAccion.Error("Ese estudiante ya tiene una actividad abierta.")
        }
        val ahora = reloj()
        val asignacion = Asignacion(
            estudianteId = estudianteId,
            actividadId = actividadId,
            asistenteId = asistenteId,
            codigo = codigoLibre(),
            creadaEn = ahora,
            venceEn = ahora + DURACION_CODIGO_MS
        )
        val id = dao.insertar(asignacion)
        return ResultadoAccion.Exito(id = id, asignacion = asignacion.copy(id = id))
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
        val nuevo = codigoLibre()
        val vence = reloj() + DURACION_CODIGO_MS
        dao.cambiarCodigo(asignacionId, nuevo, vence)
        val actualizada = dao.obtener(asignacionId)
        return ResultadoAccion.Exito(id = asignacionId, asignacion = actualizada)
    }

    private suspend fun codigoLibre(): String {
        var codigo: String
        do {
            codigo = CodigoValidator.formatear(Random.nextInt(0, 1_000_000))
        } while (dao.codigoEnUso(codigo, reloj()) > 0)
        return codigo
    }

    suspend fun validarCodigo(codigo: String): ResultadoCodigo {
        return clasificarCodigo(dao.buscarPorCodigo(codigo), reloj())
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

    /** Regla de negocio: solo la asistente asigna y anota. Devuelve su id. */
    private suspend fun exigirAsistente(): Long? {
        val sesion = auth.sesionActual.first() ?: return null
        if (sesion.rol != Rol.ASISTENTE) return null
        return sesion.id
    }
}
