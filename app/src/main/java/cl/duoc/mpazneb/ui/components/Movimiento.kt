package cl.duoc.mpazneb.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Si el sistema tiene las animaciones en cero, las pantallas nuevas tampoco animan.
 * Así se respeta la preferencia de movimiento reducido.
 */
@Composable
fun duracionAnimacion(normal: Int): Int {
    val escala = Settings.Global.getFloat(
        LocalContext.current.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    )
    return if (escala == 0f) 0 else normal
}
