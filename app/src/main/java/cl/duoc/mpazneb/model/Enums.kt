package cl.duoc.mpazneb.model

// Room guarda los enum como texto (su nombre) sin necesidad de convertidores.

enum class Rol(val etiqueta: String) {
    ADMIN("Docente / UTP"),
    ASISTENTE("Asistente de aula")
}

enum class Modulo(val etiqueta: String) {
    LECTURA("Lectura"),
    MATEMATICA("Matemática")
}

enum class EstadoActividad(val etiqueta: String) {
    BORRADOR("Borrador"),
    DISPONIBLE("Disponible"),
    DESACTIVADA("Desactivada")
}

enum class EstadoAsignacion(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_DESARROLLO("En desarrollo"),
    FINALIZADA("Finalizada")
}

enum class TipoPregunta(val etiqueta: String, val modulo: Modulo) {
    EXPLICITA("Explícita", Modulo.LECTURA),
    IMPLICITA("Implícita", Modulo.LECTURA),
    VOCABULARIO("Vocabulario", Modulo.LECTURA),
    CALCULO("Cálculo", Modulo.MATEMATICA),
    PROBLEMA("Problema", Modulo.MATEMATICA);

    companion object {
        fun de(modulo: Modulo) = entries.filter { it.modulo == modulo }
    }
}
