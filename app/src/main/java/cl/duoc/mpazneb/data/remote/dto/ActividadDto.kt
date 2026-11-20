package cl.duoc.mpazneb.data.remote.dto

data class ActividadDto(
    val id: Long,
    val nombre: String,
    val modulo: String,
    val curso: Int,
    val contenido: String,
    val objetivo: String,
    val dificultad: Int,
    val instrucciones: String,
    val intentosPermitidos: Int,
    val estado: String,
    val textoTitulo: String? = null,
    val textoCuerpo: String? = null,
    val imagenRuta: String? = null
)

data class ActividadRequest(
    val nombre: String,
    val modulo: String,
    val curso: Int,
    val contenido: String,
    val objetivo: String,
    val dificultad: Int,
    val instrucciones: String,
    val intentosPermitidos: Int,
    val estado: String,
    val textoTitulo: String? = null,
    val textoCuerpo: String? = null,
    val imagenRuta: String? = null
)

