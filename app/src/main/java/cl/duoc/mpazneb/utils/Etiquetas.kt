package cl.duoc.mpazneb.utils

/** Textos cortos que se repiten en filtros y tarjetas. */
fun etiquetaCurso(curso: Int): String = "$curso.º básico"

fun etiquetaDificultad(nivel: Int): String = when (nivel) {
    1 -> "Inicial"
    2 -> "Intermedia"
    3 -> "Avanzada"
    else -> "Nivel $nivel"
}
