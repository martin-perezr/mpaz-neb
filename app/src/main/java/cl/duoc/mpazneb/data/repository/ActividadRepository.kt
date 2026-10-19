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
import kotlinx.coroutines.flow.Flow

/** Única puerta de entrada a las actividades: los ViewModel nunca usan el DAO directamente. */
class ActividadRepository(private val dao: ActividadDao) {

    fun catalogo(): Flow<List<ActividadConConteo>> = dao.catalogo()

    fun disponibles(modulo: Modulo, curso: Int, dificultad: Int): Flow<List<Actividad>> =
        dao.disponibles(modulo, curso, dificultad)

    suspend fun obtener(id: Long): Actividad? = dao.obtener(id)

    suspend fun obtenerCompleta(id: Long): ActividadConPreguntas? = dao.obtenerCompleta(id)

    /** Inserta si es nueva (id = 0) o actualiza si ya existe. Devuelve el id. */
    suspend fun guardar(actividad: Actividad): Long =
        if (actividad.id == 0L) dao.insertar(actividad) else {
            dao.actualizar(actividad)
            actividad.id
        }

    suspend fun cambiarEstado(id: Long, estado: EstadoActividad) = dao.cambiarEstado(id, estado)

    fun preguntasDe(actividadId: Long): Flow<List<PreguntaConAlternativas>> = dao.preguntasDe(actividadId)

    suspend fun obtenerPregunta(id: Long): PreguntaConAlternativas? = dao.obtenerPregunta(id)

    suspend fun contarPreguntas(actividadId: Long): Int = dao.contarPreguntas(actividadId)

    suspend fun guardarPregunta(pregunta: Pregunta, alternativas: List<Alternativa>): Long =
        dao.guardarPreguntaCompleta(pregunta, alternativas)

    suspend fun borrarPregunta(id: Long) = dao.borrarPregunta(id)
}
