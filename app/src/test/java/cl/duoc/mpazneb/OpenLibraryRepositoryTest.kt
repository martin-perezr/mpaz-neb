package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.remote.openlibrary.OpenLibraryLibroDto
import cl.duoc.mpazneb.data.remote.openlibrary.OpenLibrarySearchResponse
import cl.duoc.mpazneb.data.remote.openlibrary.OpenLibraryService
import cl.duoc.mpazneb.data.repository.OpenLibraryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenLibraryRepositoryTest {

    private val servicio = mockk<OpenLibraryService>()
    private val repo = OpenLibraryRepository(servicio)

    @Test
    fun buscar_devuelveVacioSiLaConsultaEstaEnBlanco() = runTest {
        assertEquals(emptyList<Any>(), repo.buscar("   "))
        coVerify(exactly = 0) { servicio.buscarLibros(any()) }
    }

    @Test
    fun buscar_mapeaTitulosYAutores() = runTest {
        coEvery { servicio.buscarLibros("tomás") } returns OpenLibrarySearchResponse(
            docs = listOf(
                OpenLibraryLibroDto("El volantín de Tomás", listOf("Francisco Coloane")),
                OpenLibraryLibroDto("  ", listOf("Autor")),
            ),
        )

        val libros = repo.buscar("  tomás  ")

        assertEquals(1, libros.size)
        assertEquals("El volantín de Tomás", libros[0].titulo)
        assertEquals(listOf("Francisco Coloane"), libros[0].autores)
        coVerify { servicio.buscarLibros("tomás") }
    }

    @Test
    fun buscar_propagaFalloDeConexion() = runTest {
        coEvery { servicio.buscarLibros(any()) } throws IOException("sin internet")

        try {
            repo.buscar("poesía")
            error("Debía lanzar IOException")
        } catch (e: IOException) {
            assertTrue(e.message!!.contains("sin internet"))
        }
    }
}