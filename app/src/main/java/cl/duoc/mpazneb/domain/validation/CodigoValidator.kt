package cl.duoc.mpazneb.domain.validation

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
}
