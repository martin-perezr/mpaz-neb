package cl.duoc.mpazneb.data.remote.openlibrary

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryService {
    @GET("search.json?fields=title,author_name&limit=10")
    suspend fun buscarLibros(
        @Query("q") consulta: String
    ): OpenLibrarySearchResponse
}

data class OpenLibrarySearchResponse(
    val docs: List<OpenLibraryLibroDto>
)

data class OpenLibraryLibroDto(
    @SerializedName("title") val titulo: String?,
    @SerializedName("author_name") val autores: List<String>?
)

