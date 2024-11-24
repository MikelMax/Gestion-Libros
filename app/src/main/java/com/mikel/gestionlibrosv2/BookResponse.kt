package com.mikel.gestionlibrosv2

// Clase de datos que representa la respuesta de una API de libros
data class BookResponse(
    // Lista de elementos de libros
    val items: List<BookItem>,
    // Número total de elementos, añadido para el cálculo de páginas
    val totalItems: Int
)

// Clase de datos que representa un solo elemento de libro
data class BookItem(val volumeInfo: VolumeInfo)

// Clase de datos que representa la información del volumen de un libro
data class VolumeInfo(
    // Identificador único del libro
    val id: String,
    // Título del libro
    val title: String,
    // Lista de autores del libro
    val authors: List<String>,
    // Descripción del libro
    val description: String,
    // Enlaces de imágenes opcionales del libro
    val imageLinks: ImageLinks?,
    // Editorial opcional del libro
    val publisher: String?
)

// Clase de datos que representa los enlaces de imágenes de un libro
data class ImageLinks(
    // Enlace opcional de la imagen en miniatura
    val thumbnail: String?
)