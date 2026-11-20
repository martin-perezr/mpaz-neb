package cl.duoc.mpazneb.data.remote.dto

data class AsignacionRequest(
    val estudianteId: Long,
    val actividadId: Long,
    val asistenteId: Long
)

data class ValidarCodigoRequest(
    val codigo: String
)

data class AsignacionDto(
    val id: Long,
    val estudianteId: Long,
    val actividadId: Long,
    val asistenteId: Long,
    val codigo: String,
    val creadaEn: Long,
    val venceEn: Long,
    val estado: String
)

