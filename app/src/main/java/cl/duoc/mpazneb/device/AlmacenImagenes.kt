package cl.duoc.mpazneb.device

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

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
}
