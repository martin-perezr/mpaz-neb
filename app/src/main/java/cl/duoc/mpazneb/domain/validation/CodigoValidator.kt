package cl.duoc.mpazneb.domain.validation

object CodigoValidator {
    const val LARGO = 4

    fun validar(codigo: String): String? = when {
        codigo.length < LARGO -> "Faltan números: son $LARGO"
        codigo.length > LARGO -> "El código tiene solo $LARGO números"
        !codigo.all { it.isDigit() } -> "El código tiene solo números"
        else -> null
    }
}
