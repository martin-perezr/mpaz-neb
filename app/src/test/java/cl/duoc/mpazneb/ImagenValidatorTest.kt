package cl.duoc.mpazneb

import cl.duoc.mpazneb.domain.validation.ImagenValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImagenValidatorTest {

    @Test
    fun aceptaUnaFotoJpgLiviana() {
        assertNull(ImagenValidator.validar("image/jpeg", 200_000))
    }

    @Test
    fun aceptaJustoElTamanoMaximo() {
        assertNull(ImagenValidator.validar("image/webp", ImagenValidator.TAMANO_MAXIMO_BYTES))
    }

    @Test
    fun rechazaArchivosQueNoSonImagen() {
        assertEquals("Elige una imagen JPG, PNG o WEBP", ImagenValidator.validar("application/pdf", 1_000))
        assertEquals("Elige una imagen JPG, PNG o WEBP", ImagenValidator.validar(null, 1_000))
    }

    @Test
    fun rechazaUnaImagenVaciaOSinTamano() {
        assertEquals("No se pudo leer la imagen", ImagenValidator.validar("image/png", 0))
        assertEquals("No se pudo leer la imagen", ImagenValidator.validar("image/png", null))
    }

    @Test
    fun rechazaUnaImagenDeMasDe5Mb() {
        assertEquals(
            "La imagen pesa más de 5 MB",
            ImagenValidator.validar("image/png", ImagenValidator.TAMANO_MAXIMO_BYTES + 1)
        )
    }
}