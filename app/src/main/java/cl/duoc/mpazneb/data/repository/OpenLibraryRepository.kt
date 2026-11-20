package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.remote.openlibrary.OpenLibraryService

data class LibroEncontrado(
    val titulo: String,
    val autores: List<String>
)

class OpenLibraryRepository(
    private val servicio: OpenLibraryService
) {
    suspend fun buscar(consulta: String): List<LibroEncontrado> {
        val texto = consulta.trim()
        if (texto.isEmpty()) return emptyList()

        return servicio.buscarLibros(texto).docs.mapNotNull { libro ->
            val titulo = libro.titulo?.trim()
            if (titulo.isNullOrEmpty()) return@mapNotNull null

            LibroEncontrado(titulo, libro.autores.orEmpty())
        }
    }
}

