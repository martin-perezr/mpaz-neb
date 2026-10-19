package cl.duoc.mpazneb.domain.validation

data class ErroresLogin(val alias: String? = null, val clave: String? = null) {
    val valido: Boolean get() = alias == null && clave == null
}

object LoginValidator {

    fun validarAlias(alias: String): String? = when {
        alias.isBlank() -> "Ingresa tu alias"
        alias.any { it.isWhitespace() } -> "El alias no puede tener espacios"
        alias.length !in 4..20 -> "El alias debe tener entre 4 y 20 caracteres"
        else -> null
    }

    fun validarClave(clave: String): String? = when {
        clave.isEmpty() -> "Ingresa tu contraseña"
        clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        !clave.any { it.isDigit() } || !clave.any { it.isLetter() } -> "Debe combinar letras y números"
        else -> null
    }

    fun validar(alias: String, clave: String) = ErroresLogin(validarAlias(alias), validarClave(clave))
}
