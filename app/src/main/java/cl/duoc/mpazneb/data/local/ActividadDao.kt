package cl.duoc.mpazneb.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.ActividadConConteo
import cl.duoc.mpazneb.model.ActividadConPreguntas
import cl.duoc.mpazneb.model.Alternativa
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Pregunta
import cl.duoc.mpazneb.model.PreguntaConAlternativas
import kotlinx.coroutines.flow.Flow

// Clase abstracta (no interface) para poder escribir métodos @Transaction con cuerpo.
@Dao
abstract class ActividadDao {

    // ---------- Actividades ----------

    @Query(
        """
        SELECT a.*, (SELECT COUNT(*) FROM preguntas p WHERE p.actividadId = a.id) AS totalPreguntas
        FROM actividades a
        ORDER BY a.modulo, a.curso, a.dificultad, a.nombre
        """
    )
    abstract fun catalogo(): Flow<List<ActividadConConteo>>

    @Query(
        """
        SELECT * FROM actividades
        WHERE estado = 'DISPONIBLE' AND modulo = :modulo AND curso = :curso AND dificultad = :dificultad
        ORDER BY nombre
        """
    )
    abstract fun disponibles(modulo: Modulo, curso: Int, dificultad: Int): Flow<List<Actividad>>

    @Query("SELECT * FROM actividades WHERE id = :id")
    abstract suspend fun obtener(id: Long): Actividad?

    @Transaction
    @Query("SELECT * FROM actividades WHERE id = :id")
    abstract suspend fun obtenerCompleta(id: Long): ActividadConPreguntas?

    @Insert
    abstract suspend fun insertar(actividad: Actividad): Long

    @Update
    abstract suspend fun actualizar(actividad: Actividad)

    @Query("UPDATE actividades SET estado = :estado WHERE id = :id")
    abstract suspend fun cambiarEstado(id: Long, estado: EstadoActividad)

    // ---------- Preguntas y alternativas ----------

    @Transaction
    @Query("SELECT * FROM preguntas WHERE actividadId = :actividadId ORDER BY orden")
    abstract fun preguntasDe(actividadId: Long): Flow<List<PreguntaConAlternativas>>

    @Transaction
    @Query("SELECT * FROM preguntas WHERE id = :id")
    abstract suspend fun obtenerPregunta(id: Long): PreguntaConAlternativas?

    @Query("SELECT COUNT(*) FROM preguntas WHERE actividadId = :actividadId")
    abstract suspend fun contarPreguntas(actividadId: Long): Int

    @Insert
    abstract suspend fun insertarPregunta(pregunta: Pregunta): Long

    @Update
    abstract suspend fun actualizarPregunta(pregunta: Pregunta)

    @Query("DELETE FROM preguntas WHERE id = :id")
    abstract suspend fun borrarPregunta(id: Long)

    @Insert
    abstract suspend fun insertarAlternativas(alternativas: List<Alternativa>)

    @Query("DELETE FROM alternativas WHERE preguntaId = :preguntaId")
    abstract suspend fun borrarAlternativas(preguntaId: Long)

    /** Guarda la pregunta y reemplaza sus alternativas en una sola transacción. */
    @Transaction
    open suspend fun guardarPreguntaCompleta(pregunta: Pregunta, alternativas: List<Alternativa>): Long {
        val id = if (pregunta.id == 0L) {
            insertarPregunta(pregunta)
        } else {
            actualizarPregunta(pregunta)
            pregunta.id
        }
        borrarAlternativas(id)
        insertarAlternativas(alternativas.map { it.copy(id = 0, preguntaId = id) })
        return id
    }
}
