package cl.duoc.mpazneb.domain.validation

import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.TipoPregunta

/**
 * Valida una pregunta y sus alternativas.
 * La interfaz no decide si el formulario es válido: llama a [validar].
 */
data class ErroresPregunta(
    val enunciado: String? = null,
    val tipo: String? = null,
    val alternativas: String? = null,
    val correcta: String? = null,
    val retroalimentacion: String? = null,
    val pista: String? = null,
    val procedimiento: String? = null,
    val numeros: String? = null
) {
    val valido: Boolean
        get() = enunciado == null && tipo == null && alternativas == null && correcta == null &&
            retroalimentacion == null && pista == null && procedimiento == null && numeros == null
}

object PreguntaValidator {
    const val MIN_ALTERNATIVAS = 2
    const val MAX_ALTERNATIVAS = 4

    fun validarEnunciado(enunciado: String): String? = when {
        enunciado.isBlank() -> "Escribe el enunciado"
        enunciado.trim().length < 8 -> "El enunciado debe ser más claro"
        else -> null
    }

    fun validarTipo(tipo: TipoPregunta?, modulo: Modulo?): String? {
        if (tipo == null) return "Elige el tipo de pregunta"
        if (modulo != null && tipo.modulo != modulo) return "Ese tipo no corresponde al módulo"
        return null
    }

    /**
     * Devuelve las alternativas escritas, en orden, o un mensaje si hay un hueco
     * o si hay menos de dos.
     */
    fun leerAlternativas(textos: List<String>): Pair<List<String>?, String?> {
        val recortadas = textos.map { it.trim() }
        val ultima = recortadas.indexOfLast { it.isNotEmpty() }
        if (ultima < 0) return null to "Escribe al menos $MIN_ALTERNATIVAS alternativas"
        val usadas = recortadas.take(ultima + 1)
        if (usadas.any { it.isEmpty() }) {
            return null to "Completa las alternativas en orden, sin dejar huecos"
        }
        if (usadas.size < MIN_ALTERNATIVAS) {
            return null to "Se necesitan al menos $MIN_ALTERNATIVAS alternativas"
        }
        if (usadas.size > MAX_ALTERNATIVAS) {
            return null to "Se permiten como máximo $MAX_ALTERNATIVAS alternativas"
        }
        if (usadas.distinct().size != usadas.size) {
            return null to "Las alternativas no pueden repetirse"
        }
        return usadas to null
    }

    fun validarCorrecta(indice: Int?, cantidad: Int): String? = when {
        indice == null -> "Marca cuál alternativa es la correcta"
        indice !in 0 until cantidad -> "La respuesta correcta debe ser una alternativa escrita"
        else -> null
    }

    fun validarRetroalimentacion(texto: String): String? =
        if (texto.isBlank()) "Escribe la retroalimentación de la respuesta correcta" else null

    fun validarPista(texto: String): String? =
        if (texto.isBlank()) "Escribe una pista para el reintento" else null

    /** Matemática pide una explicación breve. Lectura no usa este campo. */
    fun validarProcedimiento(modulo: Modulo?, texto: String): String? {
        if (modulo != Modulo.MATEMATICA) return null
        if (texto.isBlank()) return "Escribe el procedimiento o la explicación"
        return null
    }

    /** Si se escribe un número, el otro también debe ser un entero entre 0 y 1000. */
    fun validarNumeros(modulo: Modulo?, numeroA: String, numeroB: String): String? {
        if (modulo != Modulo.MATEMATICA) return null
        if (numeroA.isBlank() && numeroB.isBlank()) return null
        val a = numeroA.toIntOrNull()
        val b = numeroB.toIntOrNull()
        if (a == null || b == null) return "Si usas bloques, escribe los dos números"
        if (a !in 0..1000 || b !in 0..1000) return "Usa números entre 0 y 1000"
        return null
    }

    fun validar(
        modulo: Modulo?,
        enunciado: String,
        tipo: TipoPregunta?,
        alternativas: List<String>,
        indiceCorrecta: Int?,
        retroalimentacion: String,
        pista: String,
        procedimiento: String,
        numeroA: String,
        numeroB: String
    ): ErroresPregunta {
        val (_, errorAlternativas) = leerAlternativas(alternativas)
        val cantidad = leerAlternativas(alternativas).first?.size ?: 0
        return ErroresPregunta(
            enunciado = validarEnunciado(enunciado),
            tipo = validarTipo(tipo, modulo),
            alternativas = errorAlternativas,
            correcta = if (errorAlternativas != null) null else validarCorrecta(indiceCorrecta, cantidad),
            retroalimentacion = validarRetroalimentacion(retroalimentacion),
            pista = validarPista(pista),
            procedimiento = validarProcedimiento(modulo, procedimiento),
            numeros = validarNumeros(modulo, numeroA, numeroB)
        )
    }
}
