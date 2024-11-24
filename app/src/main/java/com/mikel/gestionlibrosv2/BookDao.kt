package com.mikel.gestionlibrosv2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mikel.gestionlibrosv2.Book

// Define una interfaz DAO (Data Access Object) para interactuar con la entidad Book en la base de datos
@Dao
interface BookDao {
    // Consulta para obtener todos los libros de la tabla Book
    @Query("SELECT * FROM Book")
    fun getAllBooks(): List<Book>

    // Inserta un libro en la tabla Book
    @Insert
    fun insertBook(book: Book)
}