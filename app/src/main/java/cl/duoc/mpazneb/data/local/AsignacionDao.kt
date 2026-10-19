package cl.duoc.mpazneb.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.AsignacionDetalle
import cl.duoc.mpazneb.model.Estudiante
import cl.duoc.mpazneb.model.Observacion
import cl.duoc.mpazneb.model.Respuesta
import kotlinx.coroutines.flow.Flow

@Dao
interface AsignacionDao {

    // ---------- Estudiantes ficticios ----------

    @Query("SELECT * FROM estudiantes WHERE curso = :curso ORDER BY alias")
    fun estudiantesPorCurso(curso: Int): Flow<List<Estudiante>>

    @Query("SELECT COUNT(*) FROM estudiantes WHERE alias = :alias COLLATE NOCASE")
    suspend fun contarAlias(alias: String): Int

    @Insert
    suspend fun insertarEstudiante(estudiante: Estudiante): Long

    // ---------- Asignaciones ----------

    @Insert
    suspend fun insertar(asignacion: Asignacion): Long

    @Query("SELECT * FROM asignaciones WHERE id = :id")
    suspend fun obtener(id: Long): Asignacion?

    @Query("SELECT * FROM asignaciones WHERE codigo = :codigo ORDER BY creadaEn DESC LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): Asignacion?

    @Query("SELECT COUNT(*) FROM asignaciones WHERE codigo = :codigo AND estado != 'FINALIZADA' AND venceEn > :ahora")
    suspend fun codigoEnUso(codigo: String, ahora: Long): Int

    @Query("SELECT COUNT(*) FROM asignaciones WHERE estudianteId = :estudianteId AND estado != 'FINALIZADA' AND venceEn > :ahora")
    suspend fun sesionesAbiertas(estudianteId: Long, ahora: Long): Int

    @Query("UPDATE asignaciones SET codigo = :codigo, venceEn = :venceEn WHERE id = :id")
    suspend fun cambiarCodigo(id: Long, codigo: String, venceEn: Long)

    @Query("UPDATE asignaciones SET estado = 'EN_DESARROLLO', iniciadaEn = COALESCE(iniciadaEn, :fecha) WHERE id = :id AND estado != 'FINALIZADA'")
    suspend fun marcarEnDesarrollo(id: Long, fecha: Long)

    @Query("UPDATE asignaciones SET estado = 'FINALIZADA', finalizadaEn = :fecha WHERE id = :id")
    suspend fun finalizar(id: Long, fecha: Long)

    @Transaction
    @Query("SELECT * FROM asignaciones WHERE asistenteId = :asistenteId ORDER BY creadaEn DESC")
    fun delAsistente(asistenteId: Long): Flow<List<AsignacionDetalle>>

    @Transaction
    @Query("SELECT * FROM asignaciones WHERE id = :id")
    fun detalle(id: Long): Flow<AsignacionDetalle?>

    // ---------- Respuestas y observaciones ----------

    @Insert
    suspend fun insertarRespuesta(respuesta: Respuesta): Long

    @Query("SELECT * FROM respuestas WHERE asignacionId = :asignacionId ORDER BY fecha")
    fun respuestas(asignacionId: Long): Flow<List<Respuesta>>

    @Insert
    suspend fun insertarObservacion(observacion: Observacion): Long

    @Query("SELECT * FROM observaciones WHERE asignacionId = :asignacionId ORDER BY fecha DESC")
    fun observaciones(asignacionId: Long): Flow<List<Observacion>>
}
