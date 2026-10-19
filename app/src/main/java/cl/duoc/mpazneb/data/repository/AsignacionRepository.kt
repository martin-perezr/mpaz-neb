package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.AsignacionDao
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.AsignacionDetalle
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.Estudiante
import cl.duoc.mpazneb.model.Observacion
import cl.duoc.mpazneb.model.Respuesta
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

sealed interface ResultadoCodigo {
    data class Valido(val asignacion: Asignacion) : ResultadoCodigo
    data object NoExiste : ResultadoCodigo
    data object Vencido : ResultadoCodigo
    data object YaUsado : ResultadoCodigo
}

class AsignacionRepository(
    private val dao: AsignacionDao,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    companion object {
        const val DURACION_CODIGO_MS = 30 * 60 * 1000L
    }

    // ---------- Estudiantes ficticios ----------

    fun estudiantesPorCurso(curso: Int): Flow<List<Estudiante>> = dao.estudiantesPorCurso(curso)

    suspend fun aliasExiste(alias: String): Boolean = dao.contarAlias(alias.trim()) > 0

    suspend fun crearEstudiante(alias: String, curso: Int): Long =
        dao.insertarEstudiante(Estudiante(alias = alias.trim(), curso = curso))

    // ---------- Asignaciones ----------

    suspend fun tieneSesionAbierta(estudianteId: Long): Boolean =
        dao.sesionesAbiertas(estudianteId, reloj()) > 0

    /** Crea la asignación con un código de 4 dígitos que no esté en uso y que vence en 30 minutos. */
    suspend fun crear(estudianteId: Long, actividadId: Long, asistenteId: Long): Asignacion {
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
        return asignacion.copy(id = id)
    }

    suspend fun regenerarCodigo(asignacionId: Long): Asignacion? {
        val nuevo = codigoLibre()
        dao.cambiarCodigo(asignacionId, nuevo, reloj() + DURACION_CODIGO_MS)
        return dao.obtener(asignacionId)
    }

    private suspend fun codigoLibre(): String {
        var codigo: String
        do {
            codigo = Random.nextInt(1000, 10000).toString()
        } while (dao.codigoEnUso(codigo, reloj()) > 0)
        return codigo
    }

    suspend fun validarCodigo(codigo: String): ResultadoCodigo {
        val asignacion = dao.buscarPorCodigo(codigo) ?: return ResultadoCodigo.NoExiste
        return when {
            asignacion.estado != EstadoAsignacion.PENDIENTE -> ResultadoCodigo.YaUsado
            asignacion.venceEn < reloj() -> ResultadoCodigo.Vencido
            else -> ResultadoCodigo.Valido(asignacion)
        }
    }

    suspend fun obtener(id: Long): Asignacion? = dao.obtener(id)

    suspend fun iniciar(id: Long) = dao.marcarEnDesarrollo(id, reloj())

    suspend fun finalizar(id: Long) = dao.finalizar(id, reloj())

    fun delAsistente(asistenteId: Long): Flow<List<AsignacionDetalle>> = dao.delAsistente(asistenteId)

    fun detalle(id: Long): Flow<AsignacionDetalle?> = dao.detalle(id)

    // ---------- Respuestas y observaciones ----------

    suspend fun registrarRespuesta(asignacionId: Long, preguntaId: Long, alternativaId: Long, correcta: Boolean, intento: Int) {
        dao.insertarRespuesta(
            Respuesta(
                asignacionId = asignacionId, preguntaId = preguntaId, alternativaId = alternativaId,
                correcta = correcta, intento = intento, fecha = reloj()
            )
        )
    }

    fun respuestas(asignacionId: Long): Flow<List<Respuesta>> = dao.respuestas(asignacionId)

    suspend fun guardarObservacion(asignacionId: Long, categoria: String, texto: String) {
        dao.insertarObservacion(
            Observacion(asignacionId = asignacionId, categoria = categoria, texto = texto.trim(), fecha = reloj())
        )
    }

    fun observaciones(asignacionId: Long): Flow<List<Observacion>> = dao.observaciones(asignacionId)
}
