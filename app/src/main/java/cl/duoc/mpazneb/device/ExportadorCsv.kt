package cl.duoc.mpazneb.device

import android.content.Context
import android.net.Uri

/**
 * Recurso nativo: Storage Access Framework.
 * El usuario elige dónde guardar el archivo. La app no pide permiso de almacenamiento
 * ni elige una carpeta por su cuenta.
 */
class ExportadorCsv(private val context: Context) {

    /** Devuelve null si se guardó bien, o un mensaje si falló. */
    fun escribir(uri: Uri, contenido: String): String? {
        return try {
            val salida = context.contentResolver.openOutputStream(uri)
                ?: return "No se pudo abrir el archivo elegido"
            salida.use { flujo ->
                flujo.write(contenido.toByteArray(Charsets.UTF_8))
            }
            null
        } catch (_: Exception) {
            "No se pudo guardar el archivo. Intenta de nuevo."
        }
    }
}
