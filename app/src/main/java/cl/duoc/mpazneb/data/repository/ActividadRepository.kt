package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.ActividadDao
import cl.duoc.mpazneb.data.remote.ApiService
import cl.duoc.mpazneb.data.remote.dto.ActividadDto
import cl.duoc.mpazneb.data.remote.dto.ActividadRequest
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.ActividadConConteo
import cl.duoc.mpazneb.model.ActividadConPreguntas
import cl.duoc.mpazneb.model.Alternativa
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Pregunta
import cl.duoc.mpazneb.model.PreguntaConAlternativas
import cl.duoc.mpazneb.model.Rol
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** Única puerta de entrada a las actividades: los ViewModel nunca usan el DAO directamente. */
class ActividadRepository internal constructor(
    private val dao: ActividadDao,
    private val apiService: ApiService,
    private val esAdmin: suspend () -> Boolean
) {
    /** Constructor que usa la app: el rol sale de la sesión guardada. */
    constructor(dao: ActividadDao, auth: AuthRepository, apiService: ApiService) :
            this(dao, apiService, { auth.sesionActual.first()?.rol == Rol.ADMIN })

    fun catalogo(): Flow<List<ActividadConConteo>> = dao.catalogo()

    /**
     * Pide el catálogo al servidor y lo deja en Room. Si falla la red, Room no se toca
     * y la pantalla sigue mostrando el último catálogo guardado.
     */
    suspend fun refrescarCatalogo(): ResultadoAccion {
        val remotas = try {
            apiService.listarActividades()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ResultadoAccion.Error(
                "No se pudo conectar con el servidor. Se muestra el último catálogo guardado."
            )
        }
        val locales = dao.idsExistentes().toSet()
        val entidades = try {
            remotas
                .filterNot { esConflictoDeId(it.id, locales) }
                .map { dto -> dto.aEntidad(imagenLocal = dao.obtener(dto.id)?.imagenRuta) }
        } catch (e: IllegalArgumentException) {
            return ResultadoAccion.Error("El servidor envió actividades con datos que la app no reconoce.")
        }
        dao.upsertActividades(entidades)
        dao.borrarRemotasAusentes(ID_BASE_SERVIDOR, remotas.map { it.id })
        return ResultadoAccion.Exito()
    }

    fun disponibles(modulo: Modulo, curso: Int, dificultad: Int): Flow<List<Actividad>> =
        dao.disponibles(modulo, curso, dificultad)

    suspend fun obtener(id: Long): Actividad? = dao.obtener(id)

    suspend fun obtenerCompleta(id: Long): ActividadConPreguntas? = dao.obtenerCompleta(id)

    fun preguntasDe(actividadId: Long): Flow<List<PreguntaConAlternativas>> =
        dao.preguntasDe(actividadId)

    suspend fun obtenerPregunta(id: Long): PreguntaConAlternativas? = dao.obtenerPregunta(id)

    suspend fun contarPreguntas(actividadId: Long): Int = dao.contarPreguntas(actividadId)

    /** Crea (POST) o edita (PUT) en el servidor y deja en Room lo que este confirma. Solo administración. */
    suspend fun guardar(actividad: Actividad): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        if (actividad.estado == EstadoActividad.DISPONIBLE && preguntasParaActivar(actividad) == 0) {
            return ResultadoAccion.Error("Agrega al menos una pregunta antes de dejar la actividad disponible.")
        }
        if (esRemota(actividad.id)) return enviarYGuardar(actividad)
        dao.actualizar(actividad)
        return ResultadoAccion.Exito(id = actividad.id)
    }

    suspend fun cambiarEstado(id: Long, estado: EstadoActividad): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        if (estado == EstadoActividad.DISPONIBLE && dao.contarPreguntas(id) == 0) {
            return ResultadoAccion.Error("Agrega al menos una pregunta antes de activar la actividad.")
        }
        if (esRemota(id)) {
            val actual = dao.obtener(id) ?: return ResultadoAccion.Error("No se encontró la actividad.")
            return enviarYGuardar(actual.copy(estado = estado))
        }
        dao.cambiarEstado(id, estado)
        return ResultadoAccion.Exito(id = id)
    }

    /** Elimina un borrador. Room solo lo pierde cuando el servidor confirma con 204. */
    suspend fun eliminar(id: Long): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        val actividad = dao.obtener(id) ?: return ResultadoAccion.Error("La actividad ya no existe.")
        if (!esRemota(id)) {
            return ResultadoAccion.Error("Las actividades de ejemplo de la app no se eliminan.")
        }
        if (actividad.estado != EstadoActividad.BORRADOR) {
            return ResultadoAccion.Error("Solo se pueden eliminar actividades en borrador.")
        }
        if (dao.contarAsignaciones(id) > 0) {
            return ResultadoAccion.Error("La actividad tiene asignaciones y no se puede eliminar.")
        }
        val respuesta = try {
            apiService.eliminarActividad(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ResultadoAccion.Error(
                "No se pudo eliminar en el servidor. Revisa la conexión; la actividad se conserva."
            )
        }
        if (!respuesta.isSuccessful) {
            return ResultadoAccion.Error(mensajeDeError(respuesta.errorBody()?.string(), respuesta.code()))
        }
        dao.borrarActividad(id)
        return ResultadoAccion.Exito(id = id)
    }

    suspend fun guardarPregunta(
        pregunta: Pregunta,
        alternativas: List<Alternativa>
    ): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        val id = dao.guardarPreguntaCompleta(pregunta, alternativas)
        return ResultadoAccion.Exito(id = id)
    }

    suspend fun borrarPregunta(id: Long): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        dao.borrarPregunta(id)
        return ResultadoAccion.Exito(id = id)
    }

    private suspend fun preguntasParaActivar(actividad: Actividad): Int {
        if (actividad.id == 0L) return 0
        return dao.contarPreguntas(actividad.id)
    }

    /** Regla de negocio: solo administración modifica actividades. */
    private suspend fun exigirAdmin(): String? {
        if (esAdmin()) return null
        return "Solo la administración pedagógica puede modificar actividades."
    }

    /** POST si es nueva, PUT si ya tiene id del servidor. Room solo cambia si el servidor confirma. */
    private suspend fun enviarYGuardar(actividad: Actividad): ResultadoAccion {
        val respuesta = try {
            if (actividad.id == 0L) {
                apiService.crearActividad(actividad.aSolicitud())
            } else {
                apiService.editarActividad(actividad.id, actividad.aSolicitud())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ResultadoAccion.Error(
                "No se pudo guardar en el servidor. Revisa la conexión y vuelve a intentarlo; lo que escribiste se conserva."
            )
        }
        val dto = respuesta.body()
        if (!respuesta.isSuccessful || dto == null) {
            return ResultadoAccion.Error(mensajeDeError(respuesta.errorBody()?.string(), respuesta.code()))
        }
        val entidad = try {
            dto.aEntidad(imagenLocal = actividad.imagenRuta)
        } catch (e: IllegalArgumentException) {
            return ResultadoAccion.Error("El servidor devolvió datos que la app no reconoce.")
        }
        dao.upsertActividades(listOf(entidad))
        return ResultadoAccion.Exito(id = dto.id)
    }

    companion object {
        /** Los ids hasta este valor son de la semilla local; el servidor numera desde 1001. */
        const val ID_BASE_SERVIDOR = 1000L

        /** Ids de la semilla que también existen en el servidor por acuerdo con Hanael. */
        val IDS_COMPARTIDOS: Set<Long> = emptySet()

        /** Un id remoto que cae sobre una fila local distinta no debe pisar los datos locales. */
        fun esConflictoDeId(idRemoto: Long, idsLocales: Set<Long>): Boolean =
            idRemoto in idsLocales && idRemoto <= ID_BASE_SERVIDOR && idRemoto !in IDS_COMPARTIDOS

        /** Nuevas (id 0), con id del servidor, o compartidas por acuerdo: estas viajan al servidor. */
        fun esRemota(id: Long): Boolean =
            id == 0L || id > ID_BASE_SERVIDOR || id in IDS_COMPARTIDOS

        /** Lee {"error":"..."} de la respuesta del servidor; si no viene, usa un mensaje genérico. */
        fun mensajeDeError(cuerpo: String?, codigo: Int): String {
            val delServidor = try {
                JsonParser.parseString(cuerpo).asJsonObject.get("error")?.asString
            } catch (e: Exception) {
                null
            }
            return delServidor ?: "El servidor rechazó el guardado (código $codigo)."
        }
    }
}

/** Convierte la respuesta del servidor en la entidad de Room. Lanza IllegalArgumentException si el módulo o el estado no existen. */
fun ActividadDto.aEntidad(imagenLocal: String? = null): Actividad = Actividad(
    id = id,
    nombre = nombre,
    modulo = Modulo.valueOf(modulo),
    curso = curso,
    contenido = contenido,
    objetivo = objetivo,
    dificultad = dificultad,
    instrucciones = instrucciones,
    intentosPermitidos = intentosPermitidos,
    estado = EstadoActividad.valueOf(estado),
    textoTitulo = textoTitulo,
    textoCuerpo = textoCuerpo,
    // La imagen de galería vive solo en el teléfono: si el servidor no la trae, se conserva la local.
    imagenRuta = imagenRuta ?: imagenLocal
)

/** Datos editables que se envían al servidor. La imagen de galería no sale del teléfono. */
fun Actividad.aSolicitud(): ActividadRequest = ActividadRequest(
    nombre = nombre,
    modulo = modulo.name,
    curso = curso,
    contenido = contenido,
    objetivo = objetivo,
    dificultad = dificultad,
    instrucciones = instrucciones,
    intentosPermitidos = intentosPermitidos,
    estado = estado.name,
    textoTitulo = textoTitulo,
    textoCuerpo = textoCuerpo,
    imagenRuta = null
)