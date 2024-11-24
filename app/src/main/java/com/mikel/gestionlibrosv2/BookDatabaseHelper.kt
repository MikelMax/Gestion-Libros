package com.mikel.gestionlibrosv2

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

// Clase que ayuda a gestionar la base de datos SQLite para la entidad Book
class BookDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Metodo que se llama cuando se crea la base de datos por primera vez
    override fun onCreate(db: SQLiteDatabase) {
        // Sentencia SQL para crear la tabla Book
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_TITLE TEXT,
                $COLUMN_AUTHOR TEXT,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_COVER_URL TEXT,
                $COLUMN_PUBLISHER TEXT,
                $COLUMN_NOTES TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    // Metodo que se llama cuando se actualiza la version de la base de datos
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Elimina la tabla existente y crea una nueva
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Metodo para instertar un libro en la base de datos
    fun insertBook(book: Book): Boolean {
        val db = writableDatabase

        // Verificar si el libro ya existe por id o nombre
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID),
            "$COLUMN_ID = ? OR $COLUMN_TITLE = ?",
            arrayOf(book.id, book.title),
            null,
            null,
            null
        )

        return if (cursor.count > 0) {
            // SI el libro ya existe, no lo inserta
            Log.d("BookDatabaseHelper", "Book already exists: ${book.title}")
            cursor.close()
            false
        } else {
            cursor.close()
            // Si el libro no existe, lo inserta
            val values = ContentValues().apply {
                put(COLUMN_ID, book.id)
                put(COLUMN_TITLE, book.title)
                put(COLUMN_AUTHOR, book.author)
                put(COLUMN_DESCRIPTION, book.description)
                put(COLUMN_COVER_URL, book.coverUrl)
                put(COLUMN_PUBLISHER, book.publisher)
                put(COLUMN_NOTES, book.notes)
            }
            Log.d("BookDatabaseHelper", "Inserting book: ${book.title}, ${book.id}")
            return try {
                val result = db.insert(TABLE_NAME, null, values)
                if (result != -1L) {
                    Log.d("BookDatabaseHelper", "Book inserted: ${book.title}")
                } else {
                    Log.d("BookDatabaseHelper", "Failed to insert book: ${book.title}")
                }
                result != -1L
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("BookDatabaseHelper", "Exception inserting book: ${e.message}")
                false
            }
        }
    }

    // Metodo para obtener todos los libros de la base de datos
    fun getAllBooks(): List<Book> {
        val db = readableDatabase
        val cursor: Cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        val books = mutableListOf<Book>()
        with(cursor) {
            while (moveToNext()) {
                // Crear un objeto Book por cada fila en la tabla
                val book = Book(
                    id = getString(getColumnIndexOrThrow(COLUMN_ID)),
                    title = getString(getColumnIndexOrThrow(COLUMN_TITLE)),
                    author = getString(getColumnIndexOrThrow(COLUMN_AUTHOR)),
                    description = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    coverUrl = getString(getColumnIndexOrThrow(COLUMN_COVER_URL)),
                    publisher = getString(getColumnIndexOrThrow(COLUMN_PUBLISHER)),
                    notes = getString(getColumnIndexOrThrow(COLUMN_NOTES))
                )
                books.add(book)
                Log.d("BookDatabaseHelper", "Retrieved book: ${book.title}")
            }
        }
        cursor.close()
        return books
    }

    // Metodo para eliminar un libro de la base de datos
    fun deleteBook(book: Book): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(book.id))
        return result > 0
    }

    // Metodo para actualizar las notas de un libro en la base de datos
    fun updateBookNotes(book: Book): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOTES, book.notes)
        }
        val result = db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(book.id))
        return result > 0
    }

    companion object {
        private const val DATABASE_NAME = "books.db"
        private const val DATABASE_VERSION = 2
        const val TABLE_NAME = "books"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_COVER_URL = "cover_url"
        const val COLUMN_PUBLISHER = "publisher"
        const val COLUMN_NOTES = "notes"
    }
}