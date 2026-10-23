package cl.duoc.mpazneb.domain.validation

import cl.duoc.mpazneb.model.Modulo

/**
 * Valida el formulario de crear o editar una actividad.
 * No conoce Compose: la pantalla solo muestra los mensajes que devuelve.
 *
 * Para agregar una regla en la defensa, crea una función nueva
 * y úsala dentro de [validar].
 */
data class ErroresActividad(
    val nombre: String? = null,
    val modulo: String? = null,
    val curso: String? = null,
    val dificultad: String? = null,
    val contenido: String? = null,
    val objetivo: String? = null,
    val instrucciones: String? = null,
    val intentos: String? = null,
    val texto: String? = null
) {
    val valido: Boolean
        get() = nombre == null && modulo == null && curso == null && dificultad == null &&
            contenido == null && objetivo == null && instrucciones == null &&
            intentos == null && texto == null
}

object ActividadValidator {
    const val NOMBRE_MIN = 4
    const val NOMBRE_MAX = 60
    const val INTENTOS_MIN = 1
    const val INTENTOS_MAX = 5

    fun validarNombre(nombre: String): String? = when {
        nombre.isBlank() -> "Escribe el nombre de la actividad"
        nombre.trim().length < NOMBRE_MIN -> "El nombre debe tener al menos $NOMBRE_MIN caracteres"
        nombre.trim().length > NOMBRE_MAX -> "El nombre no puede superar $NOMBRE_MAX caracteres"
        else -> null
    }

    fun validarModulo(modulo: Modulo?): String? =
        if (modulo == null) "Elige Lectura o Matemática" else null

    fun validarCurso(curso: Int?): String? =
        if (curso == null || curso !in 1..4) "Elige un curso de 1.º a 4.º básico" else null

    fun validarDificultad(dificultad: Int?): String? =
        if (dificultad == null || dificultad !in 1..3) "Elige una dificultad" else null

    fun validarContenido(contenido: String): String? = when {
        contenido.isBlank() -> "Escribe el contenido que trabaja la actividad"
        contenido.trim().length < 4 -> "El contenido es demasiado corto"
        else -> null
    }

    fun validarObjetivo(objetivo: String): String? = when {
        objetivo.isBlank() -> "Escribe el objetivo"
        objetivo.trim().length < 8 -> "El objetivo debe explicar qué se practica"
        else -> null
    }

    fun validarInstrucciones(instrucciones: String): String? = when {
        instrucciones.isBlank() -> "Escribe las instrucciones para el estudiante"
        instrucciones.trim().length < 8 -> "Las instrucciones deben ser más claras"
        else -> null
    }

    fun validarIntentos(texto: String): String? {
        if (texto.isBlank()) return "Indica cuántos intentos tiene"
        val numero = texto.toIntOrNull() ?: return "Los intentos deben ser un número"
        if (numero !in INTENTOS_MIN..INTENTOS_MAX) {
            return "Los intentos van de $INTENTOS_MIN a $INTENTOS_MAX"
        }
        return null
    }

    /** En Lectura el texto es obligatorio. En Matemática no se usa. */
    fun validarTexto(modulo: Modulo?, texto: String): String? {
        if (modulo != Modulo.LECTURA) return null
        if (texto.isBlank()) return "La actividad de Lectura necesita un texto"
        if (texto.trim().length < 20) return "El texto debe tener al menos 20 caracteres"
        return null
    }

    fun validar(
        nombre: String,
        modulo: Modulo?,
        curso: Int?,
        dificultad: Int?,
        contenido: String,
        objetivo: String,
        instrucciones: String,
        intentos: String,
        texto: String
    ): ErroresActividad = ErroresActividad(
        nombre = validarNombre(nombre),
        modulo = validarModulo(modulo),
        curso = validarCurso(curso),
        dificultad = validarDificultad(dificultad),
        contenido = validarContenido(contenido),
        objetivo = validarObjetivo(objetivo),
        instrucciones = validarInstrucciones(instrucciones),
        intentos = validarIntentos(intentos),
        texto = validarTexto(modulo, texto)
    )
}
