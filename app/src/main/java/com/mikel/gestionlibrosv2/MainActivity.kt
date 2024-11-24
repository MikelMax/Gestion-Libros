package com.mikel.gestionlibrosv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.mikel.gestionlibrosv2.ui.theme.GestionLibrosV2Theme

// Actividad principal de la aplicación
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Tema de la aplicación
            GestionLibrosV2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Controlador de navegación
                    val navController = rememberNavController()
                    // ViewModel para la búsqueda de libros
                    val bookSearchViewModel: BookSearchViewModel = viewModel()
                    // Configuración del NavHost para la navegación
                    NavHost(navController, startDestination = "main") {
                        composable("main") { MainScreen(navController) } // Pantalla principal
                        composable("bookSearch") { BookSearchScreen(navController, bookSearchViewModel) } // Pantalla de búsqueda de libros
                        composable("addedBooks") { AddedBooksScreen(navController, bookSearchViewModel) } // Pantalla de libros añadidos
                        composable(
                            "bookDetail/{bookJson}?isBookAdded={isBookAdded}",
                            arguments = listOf(
                                navArgument("bookJson") { defaultValue = "{}" },
                                navArgument("isBookAdded") { defaultValue = "false" }
                            )
                        ) { backStackEntry ->
                            val bookJson = backStackEntry.arguments?.getString("bookJson") ?: "{}"
                            val isBookAdded = backStackEntry.arguments?.getString("isBookAdded")?.toBoolean() ?: false
                            val book = Gson().fromJson(bookJson, Book::class.java)
                            if (book != null) {
                                BookDetailScreen(navController, book, bookSearchViewModel, isBookAdded) // Pantalla de detalles del libro
                            } else {
                                Text("Book not found")
                            }
                        }
                        composable("book_notes/{bookId}") { backStackEntry ->
                            val bookId = backStackEntry.arguments?.getString("bookId")
                            val book = bookSearchViewModel.getBookById(bookId)
                            if (book != null) {
                                BookNotesScreen(navController, book, bookSearchViewModel) // Pantalla de notas del libro
                            } else {
                                Text("Book not found")
                            }
                        }
                    }
                }
            }
        }
    }

    // Composable para la pantalla principal
    @Composable
    fun MainScreen(navController: NavHostController) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { navController.navigate("bookSearch") },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Buscar libros")
                }
                Button(
                    onClick = { navController.navigate("addedBooks") },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Mis libros")
                }
            }
        }
    }
}