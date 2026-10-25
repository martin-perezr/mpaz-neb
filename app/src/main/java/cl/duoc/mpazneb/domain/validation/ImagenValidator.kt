package cl.duoc.mpazneb.domain.validation

object ImagenValidator {

    const val TAMANO_MAXIMO_BYTES: Long = 5 * 1024 * 1024L

    private val TIPOS_PERMITIDOS = setOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp",
    )

    fun validar(tipo: String?, bytes: Long?): String? = when {
        (bytes == null) || (bytes <= 0L) -> "No se pudo leer la imagen"
        (tipo == null) || (!TIPOS_PERMITIDOS.contains(tipo.lowercase())) -> "Elige una imagen JPG, PNG o WEBP"
        bytes > TAMANO_MAXIMO_BYTES -> "La imagen pesa más de 5 MB"
        else -> null
    }
}
