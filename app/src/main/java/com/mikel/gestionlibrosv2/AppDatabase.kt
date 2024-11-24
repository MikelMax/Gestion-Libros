package com.mikel.gestionlibrosv2

import androidx.room.Database
import androidx.room.RoomDatabase

// Define la base de datos de Room con una entidad (Book) y una versión (1)
@Database(entities = [Book::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    // Define un metodo abstracto para obtener el DAO de Book
    abstract fun bookDao(): BookDao
}