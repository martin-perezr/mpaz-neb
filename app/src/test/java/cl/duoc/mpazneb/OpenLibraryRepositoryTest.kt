package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.remote.openlibrary.OpenLibraryLibroDto
import cl.duoc.mpazneb.data.remote.openlibrary.OpenLibrarySearchResponse
import cl.duoc.mpazneb.data.remote.openlibrary.OpenLibraryService
import cl.duoc.mpazneb.data.repository.LibroEncontrado
import cl.duoc.mpazneb.data.repository.OpenLibraryRepository
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class OpenLibraryRepositoryTest {

    @Test
    fun convierteLosResultadosEnLibros() = runBlocking {
        val repositorio = crearRepositorio { consulta ->
            assertEquals("El principito", consulta)
            OpenLibrarySearchResponse(
                listOf(
                    OpenLibraryLibroDto(" El Principito ", listOf("Antoine de Saint-Exupéry")),
                    OpenLibraryLibroDto("Libro sin autor", null),
                    OpenLibraryLibroDto(null, listOf("Otro autor")),
                    OpenLibraryLibroDto("   ", null)
                )
            )
        }

        val resultado = repositorio.buscar("  El principito  ")

        assertEquals(
            listOf(
                LibroEncontrado("El Principito", listOf("Antoine de Saint-Exupéry")),
                LibroEncontrado("Libro sin autor", emptyList())
            ),
            resultado
        )
    }

    @Test
    fun devuelveListaVaciaCuandoNoHayCoincidencias() = runBlocking {
        val repositorio = crearRepositorio {
            OpenLibrarySearchResponse(emptyList())
        }

        assertEquals(emptyList<LibroEncontrado>(), repositorio.buscar("Sin coincidencias"))
    }

    @Test
    fun evitaConsultarSiLaBusquedaEstaVacia() = runBlocking {
        val repositorio = crearRepositorio {
            error("Una búsqueda vacía no debería llamar al servicio")
        }

        assertEquals(emptyList<LibroEncontrado>(), repositorio.buscar("   "))
    }

    @Test
    fun propagaElErrorDeConexion() {
        val fallo = IOException("Sin conexión")
        val repositorio = crearRepositorio { throw fallo }

        val resultado = assertThrows(IOException::class.java) {
            runBlocking { repositorio.buscar("El principito") }
        }
        assertSame(fallo, resultado)
    }

    private fun crearRepositorio(
        responder: (String) -> OpenLibrarySearchResponse
    ): OpenLibraryRepository {
        val servicio = object : OpenLibraryService {
            override suspend fun buscarLibros(consulta: String): OpenLibrarySearchResponse =
                responder(consulta)
        }
        return OpenLibraryRepository(servicio)
    }
}