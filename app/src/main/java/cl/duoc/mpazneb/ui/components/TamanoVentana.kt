package cl.duoc.mpazneb.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Tamaño de la ventana con los mismos cortes que WindowSizeClass de Material:
 * COMPACTA < 600 dp (teléfono), MEDIA < 840 dp (tablet vertical), EXPANDIDA (tablet horizontal).
 * Se recalcula solo al girar el dispositivo.
 */
enum class TamanoVentana { COMPACTA, MEDIA, EXPANDIDA }

@Composable
fun rememberTamanoVentana(): TamanoVentana {
    val ancho = LocalConfiguration.current.screenWidthDp
    return when {
        ancho < 600 -> TamanoVentana.COMPACTA
        ancho < 840 -> TamanoVentana.MEDIA
        else -> TamanoVentana.EXPANDIDA
    }
}
