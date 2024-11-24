package com.mikel.gestionlibrosv2

import android.app.Application
import androidx.room.Room

// Clase MyApp que extiende de Application para inicializar la base de datos
class MyApp : Application() {
    // Propiedad lateinit para la base de datos
    lateinit var database: AppDatabase

    // Método onCreate que se llama cuando la aplicación se crea
    override fun onCreate() {
        super.onCreate()
        // Inicialización de la base de datos usando Room
        database = Room.databaseBuilder(
            applicationContext, // Contexto de la aplicación
            AppDatabase::class.java, // Clase de la base de datos
            "app-database" // Nombre de la base de datos
        ).build() // Construcción de la base de datos
    }
}