package com.mikel.gestionlibrosv2

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Interfaz que define el servicio de la API de Google Books
interface GoogleBooksApi {
    // Metodo para buscar libros en la API de Google Books
    @GET("volumes")
    fun searchBooks(
        // Parametro de consulta para la busqueda de libros
        @Query("q") query: String,
        // Parametro que restringe los resultados por el idioma
        @Query("langRestrict") langRestrict: String,
        // Parametro que ordena los resultados por relevancia
        @Query("OrderBy") OrderBy: String = "relevance",
        // Paarametro que limita el numero de resultados a 33 (40 maximo)
        @Query("maxResults") maxResults: Int = 33

    ): Call<BookResponse> // Devuelve una llamada que contiene la respuesta de la busqueda de libros
}