package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.remote.dto.ActividadDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Test

class ActividadDtoGsonTest {

    @Test
    fun gsonLeeListadoComoSpringBoot() {
        val json = """
            [
              {
                "id": 1009,
                "nombre": "El volantín de Tomás",
                "modulo": "LECTURA",
                "curso": 2,
                "contenido": "Actividad de lectura",
                "objetivo": "Comprensión lectora",
                "dificultad": 1,
                "instrucciones": "Lee y responde las preguntas",
                "intentosPermitidos": 2,
                "estado": "DISPONIBLE",
                "textoTitulo": "El volantín de Tomás",
                "textoCuerpo": "Contenido de demostración.",
                "imagenRuta": null
              }
            ]
        """.trimIndent()
        val tipo = object : TypeToken<List<ActividadDto>>() {}.type
        val lista: List<ActividadDto> = Gson().fromJson(json, tipo)
        assertEquals(1, lista.size)
        assertEquals(1009L, lista[0].id)
        assertEquals("LECTURA", lista[0].modulo)
    }
}