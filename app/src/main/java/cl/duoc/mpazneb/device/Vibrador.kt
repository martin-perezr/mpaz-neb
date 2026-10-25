package cl.duoc.mpazneb.device

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Recurso nativo: vibración. Refuerzo no visual al enviar y al acertar.
 * Usa el permiso VIBRATE (normal: Android lo concede al instalar, declarado en el manifiesto).
 */
class Vibrador(context: Context) {

    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }

    fun pulsoCorto() = unPulso(40)

    fun exito() {
        if (vibrator?.hasVibrator() == true) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 80, 50), -1))
        }
    }

    private fun unPulso(milisegundos: Long) {
        if (vibrator?.hasVibrator() == true) {
            vibrator.vibrate(VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}
