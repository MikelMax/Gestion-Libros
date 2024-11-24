package com.mikel.gestionlibrosv2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Objeto que proporciona una instancia de Retrofit configurada para la API de Google Books
object RetrofitInstance {
    private const val BASE_URL = "https://www.googleapis.com/books/v1/" // URL base de la API

    // Propiedad lazy que inicializa Retrofit y crea la implementación de la interfaz GoogleBooksApi
    val api: GoogleBooksApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Establece la URL base
            .addConverterFactory(GsonConverterFactory.create()) // Añade el convertidor de Gson
            .build() // Construye la instancia de Retrofit
            .create(GoogleBooksApi::class.java) // Crea la implementación de la interfaz GoogleBooksApi
    }
}