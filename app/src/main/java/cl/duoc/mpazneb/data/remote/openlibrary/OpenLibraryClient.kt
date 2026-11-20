package cl.duoc.mpazneb.data.remote.openlibrary

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenLibraryClient {
    private const val BASE_URL = "https://openlibrary.org/"

    val servicio: OpenLibraryService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenLibraryService::class.java)
    }
}

