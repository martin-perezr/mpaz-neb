package cl.duoc.mpazneb.domain

/**
 * Arma un resultado descriptivo. No calcula notas ni porcentajes oficiales.
 * Durante la defensa se puede cambiar el texto de cada caso en [frase].
 */
object ResultadoDescriptivo {

    fun frase(correctas: Int, total: Int): String = when {
        total <= 0 -> "Todavía no hay preguntas en esta actividad. No es una nota."
        correctas >= total -> "Logro alto: respondió correctamente todas las preguntas. No es una nota."
        correctas <= 0 -> "En inicio: todavía no registra respuestas correctas. No es una nota."
        else -> "Logro parcial: respondió correctamente $correctas de $total preguntas. No es una nota."
    }
}
