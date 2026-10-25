package cl.duoc.mpazneb.domain.validation

/**
 * Valida una observación ficticia. No guarda antecedentes reales ni diagnósticos.
 * Para agregar una categoría en la defensa, súmala a [CATEGORIAS].
 */
data class ErroresObservacion(
    val categoria: String? = null,
    val texto: String? = null
) {
    val valido: Boolean get() = categoria == null && texto == null
}

object ObservacionValidator {
    val CATEGORIAS = listOf("Participación", "Dificultad", "Ánimo", "Otra")
    const val TEXTO_MIN = 8
    const val TEXTO_MAX = 200

    fun validarCategoria(categoria: String): String? =
        if (categoria !in CATEGORIAS) "Elige una categoría" else null

    fun validarTexto(texto: String): String? = when {
        texto.isBlank() -> "Escribe la observación ficticia"
        texto.trim().length < TEXTO_MIN -> "La observación debe tener al menos $TEXTO_MIN caracteres"
        texto.trim().length > TEXTO_MAX -> "La observación no puede superar $TEXTO_MAX caracteres"
        else -> null
    }

    fun validar(categoria: String, texto: String) =
        ErroresObservacion(validarCategoria(categoria), validarTexto(texto))
}
