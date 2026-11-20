package cl.duoc.mpazneb

import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.mpazneb.data.remote.RetrofitClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaludApiInstrumentedTest {

    @Test
    fun backendRespondeOk() = runBlocking {
        val respuesta = RetrofitClient.api.salud()
        assertEquals("ok", respuesta.estado)
    }
}

