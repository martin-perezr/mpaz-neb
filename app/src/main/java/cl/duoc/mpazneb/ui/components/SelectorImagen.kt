package cl.duoc.mpazneb.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import java.io.File

/**
 * Permiso de lectura de imágenes según la versión de Android:
 * hasta Android 12 es READ_EXTERNAL_STORAGE; desde Android 13 es READ_MEDIA_IMAGES,
 * y desde Android 14 el usuario puede dar acceso solo a las fotos que elija.
 */
fun permisosGaleria(): Array<String> = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

/**
 * Recurso nativo: galería. Flujo:
 * 1. Si no hay permiso, se pide. Si lo rechazan, se explica cómo activarlo y no se abre nada.
 * 2. Con permiso se abre el selector de fotos del sistema (Photo Picker), solo imágenes.
 * 3. La foto elegida se muestra recortada, con borde redondeado y alto fijo.
 */
@Composable
fun SelectorImagen(
    ruta: String?,
    error: String?,
    onElegida: (Uri) -> Unit,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var permisoRechazado by remember { mutableStateOf(false) }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onElegida(uri) // null = la persona cerró la galería sin elegir
    }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { respuesta ->
        permisoRechazado = respuesta.values.none { it }
        if (!permisoRechazado) galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun abrirGaleria() {
        val concedido = permisosGaleria().any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (concedido) {
            permisoRechazado = false
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            pedirPermiso.launch(permisosGaleria())
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ilustración (opcional)", style = MaterialTheme.typography.titleMedium)
        if (ruta != null) {
            AsyncImage(
                model = File(ruta),
                contentDescription = "Ilustración de la actividad",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { abrirGaleria() }) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (ruta == null) "Elegir de la galería" else "Cambiar imagen")
            }
            if (ruta != null) {
                TextButton(onClick = onQuitar) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Quitar")
                }
            }
        }
        val mensaje = if (permisoRechazado) {
            "Sin permiso no podemos abrir tus fotos. Puedes activarlo en Ajustes > Apps > MPAZ NEB > Permisos."
        } else {
            error
        }
        if (mensaje != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text(mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}