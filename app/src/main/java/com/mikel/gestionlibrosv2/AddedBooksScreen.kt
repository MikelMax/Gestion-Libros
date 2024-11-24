package com.mikel.gestionlibrosv2

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.gson.Gson
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddedBooksScreen(navController: NavController, viewModel: BookSearchViewModel = viewModel()) {
    //Observa la lista de libros añadidos desde el viewModel
    val addedBooks by viewModel.addedBooks.observeAsState(emptyList())
    //Variable de estado para almaceanr el texto de busqueda
    var searchQuery by remember { mutableStateOf("") }
    //Filtra los libros segun el texto de busqueda
    val filteredBooks = addedBooks.filter { it.title.contains(searchQuery, ignoreCase = true) || it.author.contains(searchQuery, ignoreCase = true) }

    //Columna principal que contiene todos los elementos de la pantalla
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        //Campo de texto para la busqueda de libros
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar libros") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    //Acción de busqueda
                }
            )
        )
        //Titulo en la secchion de libros añadidos
        Text("Libros Añadidos", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        //LazyColumn que muestra los libros filtrados
        LazyColumn {
            items(filteredBooks) { book ->
                //Filtra que representa cada libro
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        //Convierte el libro a JSON y navega a la pantalla de detalles
                        val bookJson = Gson().toJson(book)
                        val encodedBookJson = Uri.encode(bookJson)
                        navController.navigate("bookDetail/$encodedBookJson?isBookAdded=true")
                    }
                    .padding(8.dp)) {
                    //Caja que contiene la imagen de portada del libro
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp, 72.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = rememberImagePainter(data = book.coverUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    //Columna que contiene el titulo y autor del libro
                    Column(
                        modifier = Modifier
                            .weight(3f)
                            .padding(8.dp)
                    ) {
                        Text(
                            book.title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            book.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}