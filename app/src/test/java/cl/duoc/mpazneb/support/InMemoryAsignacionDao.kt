package cl.duoc.mpazneb.support

import cl.duoc.mpazneb.data.local.AsignacionDao
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.AsignacionDetalle
import cl.duoc.mpazneb.model.Estudiante
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.Observacion
import cl.duoc.mpazneb.model.Respuesta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** DAO en memoria para pruebas unitarias de [cl.duoc.mpazneb.data.repository.AsignacionRepository]. */
class InMemoryAsignacionDao : AsignacionDao {
    private var nextAsignacionId = 1L
    private var nextEstudianteId = 1L
    val asignaciones = mutableListOf<Asignacion>()
    val estudiantes = mutableListOf<Estudiante>()
  var asistentesRegistrados = setOf(2L)

    override fun estudiantesPorCurso(curso: Int): Flow<List<Estudiante>> =
        flowOf(estudiantes.filter { it.curso == curso })

    override suspend fun contarAlias(alias: String): Int =
        estudiantes.count { it.alias.equals(alias, ignoreCase = true) }

    override suspend fun obtenerEstudiante(id: Long): Estudiante? =
        estudiantes.firstOrNull { it.id == id }

    override suspend fun existeAsistente(id: Long): Int =
        if (id in asistentesRegistrados) 1 else 0

    override suspend fun insertarEstudiante(estudiante: Estudiante): Long {
        val id = nextEstudianteId++
        estudiantes += estudiante.copy(id = id)
        return id
    }

    override suspend fun insertar(asignacion: Asignacion): Long {
        val id = if (asignacion.id == 0L) nextAsignacionId++ else asignacion.id
        val guardada = asignacion.copy(id = id)
        asignaciones.removeAll { it.id == id }
        asignaciones += guardada
        return id
    }

    override suspend fun obtener(id: Long): Asignacion? =
        asignaciones.firstOrNull { it.id == id }

    override suspend fun buscarPorCodigo(codigo: String): Asignacion? =
        asignaciones.filter { it.codigo == codigo }.maxByOrNull { it.creadaEn }

    override suspend fun asignacionVigente(estudianteId: Long, actividadId: Long, ahora: Long): Asignacion? =
        asignaciones
            .filter {
                it.estudianteId == estudianteId &&
                    it.actividadId == actividadId &&
                    it.estado != EstadoAsignacion.FINALIZADA &&
                    it.venceEn > ahora
            }
            .maxByOrNull { it.creadaEn }

    override suspend fun codigoEnUso(codigo: String, ahora: Long): Int =
        asignaciones.count {
            it.codigo == codigo && it.estado != EstadoAsignacion.FINALIZADA && it.venceEn > ahora
        }

    override suspend fun sesionesAbiertas(estudianteId: Long, ahora: Long): Int =
        asignaciones.count {
            it.estudianteId == estudianteId &&
                it.estado != EstadoAsignacion.FINALIZADA &&
                it.venceEn > ahora
        }

    override suspend fun cambiarCodigo(id: Long, codigo: String, venceEn: Long) {
        val idx = asignaciones.indexOfFirst { it.id == id }
        if (idx >= 0) {
            asignaciones[idx] = asignaciones[idx].copy(codigo = codigo, venceEn = venceEn)
        }
    }

    override suspend fun marcarEnDesarrollo(id: Long, fecha: Long) {
        val idx = asignaciones.indexOfFirst { it.id == id }
        if (idx >= 0 && asignaciones[idx].estado != EstadoAsignacion.FINALIZADA) {
            val actual = asignaciones[idx]
            asignaciones[idx] = actual.copy(
                estado = EstadoAsignacion.EN_DESARROLLO,
                iniciadaEn = actual.iniciadaEn ?: fecha
            )
        }
    }

    override suspend fun finalizar(id: Long, fecha: Long) {
        val idx = asignaciones.indexOfFirst { it.id == id }
        if (idx >= 0) {
            asignaciones[idx] = asignaciones[idx].copy(
                estado = EstadoAsignacion.FINALIZADA,
                finalizadaEn = fecha
            )
        }
    }

    override fun delAsistente(asistenteId: Long): Flow<List<AsignacionDetalle>> =
        flowOf(emptyList())

    override fun detalle(id: Long): Flow<AsignacionDetalle?> = flowOf(null)

    override suspend fun insertarRespuesta(respuesta: Respuesta): Long = respuesta.id

    override fun respuestas(asignacionId: Long): Flow<List<Respuesta>> = flowOf(emptyList())

    override suspend fun insertarObservacion(observacion: Observacion): Long = observacion.id

    override fun observaciones(asignacionId: Long): Flow<List<Observacion>> = flowOf(emptyList())
}