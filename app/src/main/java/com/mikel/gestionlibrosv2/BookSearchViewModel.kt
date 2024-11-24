package com.mikel.gestionlibrosv2

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ViewModel para gestionar la búsqueda y manipulación de libros
class BookSearchViewModel(application: Application) : AndroidViewModel(application) {
    // Helper para la base de datos
    private val dbHelper = BookDatabaseHelper(application)
    // LiveData para los libros añadidos
    val addedBooks = MutableLiveData<List<Book>>()
    // LiveData para indicar si un libro fue añadido
    val bookAdded = MutableLiveData<Boolean>()
    // LiveData para indicar si un libro ya estaba añadido
    val bookAlreadyAdded = MutableLiveData<Boolean>()
    // LiveData para los resultados de búsqueda
    val searchResults = MutableLiveData<List<Book>>()

    init {
        loadAddedBooks() // Cargar los libros añadidos al inicializar
    }

    // Cargar los libros añadidos desde la base de datos
    private fun loadAddedBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            val books = dbHelper.getAllBooks()
            Log.d("BookSearchViewModel", "Loaded books: ${books.size}")
            books.forEach { book ->
                Log.d("BookSearchViewModel", "Loaded book: ${book.title}")
            }
            addedBooks.postValue(books)
        }
    }

    // Añadir un libro a la base de datos
    fun addBook(book: Book) {
        val validBook = if (book.id == "Unknown ID" || book.id.isBlank()) {
            book.copy(id = generateUniqueId()) // Generar un ID único si el libro no tiene uno
        } else {
            book
        }

        viewModelScope.launch(Dispatchers.IO) {
            Log.d("BookSearchViewModel", "Attempting to add book: ${validBook.title}, ${validBook.id}")
            val existingBooks = dbHelper.getAllBooks()
            if (existingBooks.any { it.id == validBook.id || it.title == validBook.title }) {
                bookAlreadyAdded.postValue(true) // Indicar que el libro ya estaba añadido
                Log.d("BookSearchViewModel", "Book already added: ${validBook.title}")
            } else {
                val success = dbHelper.insertBook(validBook)
                if (success) {
                    Log.d("BookSearchViewModel", "Book added successfully: ${validBook.title}")
                    loadAddedBooks() // Recargar los libros añadidos
                    bookAdded.postValue(true) // Indicar que el libro fue añadido con éxito
                } else {
                    Log.d("BookSearchViewModel", "Failed to add book: ${validBook.title}")
                }
            }
        }
    }

    // Eliminar un libro de la base de datos
    fun deleteBook(book: Book, navController: NavController) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = dbHelper.deleteBook(book)
            if (success) {
                loadAddedBooks() // Recargar los libros añadidos
                withContext(Dispatchers.Main) {
                    navController.popBackStack() // Navegar hacia atrás
                }
            }
        }
    }

    // Actualizar las notas de un libro en la base de datos
    fun updateBookNotes(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = dbHelper.updateBookNotes(book)
            if (success) {
                loadAddedBooks() // Recargar los libros añadidos
            }
        }
    }

    // Obtener un libro por su ID
    fun getBookById(bookId: String?): Book? {
        return dbHelper.getAllBooks().find { it.id == bookId }
    }

    // Generar un ID único para un libro
    fun generateUniqueId(): String {
        return java.util.UUID.randomUUID().toString()
    }
}