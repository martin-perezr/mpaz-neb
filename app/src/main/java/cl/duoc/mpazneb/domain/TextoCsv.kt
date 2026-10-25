package cl.duoc.mpazneb.domain

/** Una respuesta ya traducida a texto, lista para mostrar o exportar. */
data class FilaResultado(
    val enunciado: String,
    val elegida: String,
    val correcta: Boolean,
    val intento: Int
)

/** Observación ficticia lista para el archivo. */
data class FilaObservacion(
    val categoria: String,
    val texto: String
)

/**
 * Construye un CSV sencillo separado por punto y coma.
 * No escribe archivos: eso lo hace el recurso nativo de exportación.
 */
object TextoCsv {

    fun escapar(valor: String): String {
        val limpio = valor.replace("\"", "\"\"")
        return "\"$limpio\""
    }

    fun documento(
        estudiante: String,
        actividad: String,
        codigo: String,
        estado: String,
        resultado: String,
        respuestas: List<FilaResultado>,
        observaciones: List<FilaObservacion>
    ): String {
        val lineas = mutableListOf<String>()
        lineas += fila("estudiante", "actividad", "codigo", "estado", "resultado")
        lineas += fila(estudiante, actividad, codigo, estado, resultado)
        lineas += ""
        lineas += fila("pregunta", "respuesta_elegida", "es_correcta", "intento")
        respuestas.forEach { respuesta ->
            val marca = if (respuesta.correcta) "si" else "no"
            lineas += fila(respuesta.enunciado, respuesta.elegida, marca, respuesta.intento.toString())
        }
        lineas += ""
        lineas += fila("categoria", "observacion_ficticia")
        observaciones.forEach { observacion ->
            lineas += fila(observacion.categoria, observacion.texto)
        }
        // El BOM ayuda a que Excel reconozca tildes y eñes.
        return "\uFEFF" + lineas.joinToString("\r\n")
    }

    private fun fila(vararg celdas: String): String = celdas.joinToString(";") { escapar(it) }
}
