package cl.duoc.mpazneb.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Fecha corta para tarjetas y resultados. Ejemplo: 22/10/2026 21:05. */
fun formatoFecha(millis: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL"))
    return formato.format(Date(millis))
}
