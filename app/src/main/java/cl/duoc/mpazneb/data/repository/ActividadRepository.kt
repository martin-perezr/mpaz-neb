package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.ActividadDao
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.ActividadConConteo
import cl.duoc.mpazneb.model.ActividadConPreguntas
import cl.duoc.mpazneb.model.Alternativa
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Pregunta
import cl.duoc.mpazneb.model.PreguntaConAlternativas
import cl.duoc.mpazneb.model.Rol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** Única puerta de entrada a las actividades: los ViewModel nunca usan el DAO directamente. */
class ActividadRepository(
    private val dao: ActividadDao,
    private val auth: AuthRepository
) {

    fun catalogo(): Flow<List<ActividadConConteo>> = dao.catalogo()

    fun disponibles(modulo: Modulo, curso: Int, dificultad: Int): Flow<List<Actividad>> =
        dao.disponibles(modulo, curso, dificultad)

    suspend fun obtener(id: Long): Actividad? = dao.obtener(id)

    suspend fun obtenerCompleta(id: Long): ActividadConPreguntas? = dao.obtenerCompleta(id)

    fun preguntasDe(actividadId: Long): Flow<List<PreguntaConAlternativas>> = dao.preguntasDe(actividadId)

    suspend fun obtenerPregunta(id: Long): PreguntaConAlternativas? = dao.obtenerPregunta(id)

    suspend fun contarPreguntas(actividadId: Long): Int = dao.contarPreguntas(actividadId)

    /** Inserta si es nueva (id = 0) o actualiza si ya existe. Solo administración. */
    suspend fun guardar(actividad: Actividad): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        if (actividad.estado == EstadoActividad.DISPONIBLE && preguntasParaActivar(actividad) == 0) {
            return ResultadoAccion.Error("Agrega al menos una pregunta antes de dejar la actividad disponible.")
        }
        val id = if (actividad.id == 0L) {
            dao.insertar(actividad)
        } else {
            dao.actualizar(actividad)
            actividad.id
        }
        return ResultadoAccion.Exito(id = id)
    }

    suspend fun cambiarEstado(id: Long, estado: EstadoActividad): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        if (estado == EstadoActividad.DISPONIBLE && dao.contarPreguntas(id) == 0) {
            return ResultadoAccion.Error("Agrega al menos una pregunta antes de activar la actividad.")
        }
        dao.cambiarEstado(id, estado)
        return ResultadoAccion.Exito(id = id)
    }

    suspend fun guardarPregunta(pregunta: Pregunta, alternativas: List<Alternativa>): ResultadoAccion {
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
        val sesion = auth.sesionActual.first()
        if (sesion?.rol == Rol.ADMIN) return null
        return "Solo la administración pedagógica puede modificar actividades."
    }
}
