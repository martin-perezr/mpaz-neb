package cl.duoc.mpazneb.domain.validation

sealed interface ResultadoPegarCodigo {
    data class Aceptado(val codigo: String) : ResultadoPegarCodigo
    data class Rechazado(val mensaje: String) : ResultadoPegarCodigo
}

object CodigoValidator {
    const val LARGO = 6

    /**
     * Deja el número en seis dígitos. Ejemplo: 7 se convierte en "000007".
     * Durante la defensa se puede cambiar [LARGO] y revisar esta función.
     */
    fun formatear(numero: Int): String {
        val limitado = numero.coerceIn(0, 999_999)
        return limitado.toString().padStart(LARGO, '0')
    }

    fun validar(codigo: String): String? = when {
        codigo.isEmpty() -> "Ingresa el código de $LARGO números"
        codigo.length < LARGO -> "Faltan números: son $LARGO"
        codigo.length > LARGO -> "El código tiene solo $LARGO números"
        !codigo.all { it.isDigit() } -> "El código tiene solo números"
        else -> null
    }

    /** Interpreta el texto del portapapeles sin modificar el código actual hasta confirmar validez. */
    fun interpretarPortapapeles(texto: String): ResultadoPegarCodigo {
        val limpio = texto.trim()
        if (limpio.isEmpty()) {
            return ResultadoPegarCodigo.Rechazado("No hay ningún código copiado")
        }
        if (limpio.length != LARGO || !limpio.all { it.isDigit() }) {
            return ResultadoPegarCodigo.Rechazado("Copia un código de 6 números para pegarlo")
        }
        return ResultadoPegarCodigo.Aceptado(limpio)
    }
}
