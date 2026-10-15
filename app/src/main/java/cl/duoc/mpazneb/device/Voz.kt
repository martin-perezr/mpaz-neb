package cl.duoc.mpazneb.device

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Recurso nativo: síntesis de voz (Text-to-Speech). Lee en voz alta instrucciones y preguntas
 * para quien todavía no lee con fluidez. Usa el parlante, no el micrófono: no requiere permisos.
 */
class Voz(context: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context.applicationContext, this)
    private var lista = false

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val chile = tts.setLanguage(Locale.forLanguageTag("es-CL"))
        lista = if (chile >= TextToSpeech.LANG_AVAILABLE) true
        else tts.setLanguage(Locale.forLanguageTag("es")) >= TextToSpeech.LANG_AVAILABLE
        tts.setSpeechRate(0.9f)
    }

    fun hablar(texto: String) {
        if (lista) tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "mpazneb")
    }

    fun detener() {
        tts.stop()
    }

    fun liberar() {
        tts.stop()
        tts.shutdown()
    }
}

/** Crea la voz al entrar a la pantalla y la libera al salir (evita fugas de memoria). */
@Composable
fun rememberVoz(): Voz {
    val context = LocalContext.current
    val voz = remember { Voz(context) }
    DisposableEffect(voz) {
        onDispose { voz.liberar() }
    }
    return voz
}
