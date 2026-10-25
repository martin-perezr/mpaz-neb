package cl.duoc.mpazneb.domain.validation

import cl.duoc.mpazneb.model.Modulo

/**
 * Comprueba que la asistente haya elegido módulo, curso, dificultad y estudiante.
 * La aplicación no elige el nivel pedagógico sola.
 */
object AsignacionFormValidator {

    fun validarFiltro(modulo: Modulo?, curso: Int?, dificultad: Int?): String? {
        if (modulo == null) return "Elige el módulo"
        if (curso == null) return "Elige el curso"
        if (dificultad == null) return "Elige la dificultad"
        return null
    }

    fun validarEstudiante(estudianteId: Long?): String? =
        if (estudianteId == null) "Elige un estudiante ficticio" else null
}
