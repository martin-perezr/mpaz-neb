package cl.duoc.mpazneb.ui.navigation

/** Todas las rutas de la app. Las que llevan {id} reciben un argumento numérico. */
object Rutas {
    const val BIENVENIDA = "bienvenida"
    const val LOGIN = "login"
    const val CODIGO = "codigo"

    const val CATALOGO = "admin/catalogo"
    const val EDITOR_ACTIVIDAD = "admin/actividad/{actividadId}"
    const val EDITOR_PREGUNTA = "admin/actividad/{actividadId}/pregunta/{preguntaId}"

    const val ASISTENTE_INICIO = "asistente/inicio"
    const val ASIGNAR = "asistente/asignar"
    const val RESULTADOS = "asistente/resultados/{asignacionId}"

    const val SESION = "estudiante/sesion/{asignacionId}"

    /** id = 0 significa "actividad nueva". */
    fun editorActividad(id: Long) = "admin/actividad/$id"
    fun editorPregunta(actividadId: Long, preguntaId: Long) = "admin/actividad/$actividadId/pregunta/$preguntaId"
    fun resultados(asignacionId: Long) = "asistente/resultados/$asignacionId"
    fun sesion(asignacionId: Long) = "estudiante/sesion/$asignacionId"
}
