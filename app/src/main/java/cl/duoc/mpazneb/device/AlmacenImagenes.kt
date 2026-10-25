package cl.duoc.mpazneb.device

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import android.provider.OpenableColumns

/**
 * La URI que entrega la galería es temporal. Copiamos la imagen a la carpeta privada
 * de la app para que siga disponible después de cerrar y abrir la app.
 */
class AlmacenImagenes(private val context: Context) {

    suspend fun copiarAInterno(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val carpeta = File(context.filesDir, "ilustraciones").apply { mkdirs() }
            val destino = File(carpeta, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: return@runCatching null
            destino.absolutePath
        }.getOrNull()
    }

    fun borrar(ruta: String?) {
        if (ruta != null) runCatching { File(ruta).delete() }
    }

    // Tipo y tamaño de la imagen elegida, para validarla antes de copiarla.
    suspend fun describir(uri: Uri): Pair<String?, Long?> = withContext(Dispatchers.IO) {
        val tipo = context.contentResolver.getType(uri)
        val bytes = runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
                if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else null
            }
        }.getOrNull()
        tipo to bytes
    }
}
