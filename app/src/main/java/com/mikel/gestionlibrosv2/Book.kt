package com.mikel.gestionlibrosv2

import androidx.room.Entity
import androidx.room.PrimaryKey

// Define una entidad de base de datos llamada Book
@Entity(tableName = "book")
data class Book(
    // Define la PrimaryKey de la entidad
    @PrimaryKey val id: String,
    // Titulo del libro
    val title: String,
    // Autor del libro
    val author: String,
    // Descripción del libro
    val description: String,
    // URL de la portada del libro
    val coverUrl: String,
    // Editorial del libro
    val publisher: String,
    // Notas del libro(puede ser nulo)
    val notes: String?
)