package com.mikel.gestionlibrosv2

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList

// Composable para mostrar una imagen usando OkHttp
@Composable
fun OkHttpImage(url: String, modifier: Modifier = Modifier) {
    val painter = rememberImagePainter(
        data = url,
        builder = {
            placeholder(R.drawable.predeterminada)
            error(R.drawable.predeterminada)
            crossfade(true)
            size(coil.size.Size.ORIGINAL) // Usa el tamaño original de la imagen
        }
    )

    Box(modifier = modifier) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// Composable principal para la pantalla de búsqueda de libros
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSearchScreen(navController: NavController, viewModel: BookSearchViewModel = viewModel()) {
    // Estado para la consulta de búsqueda
    val query = remember { mutableStateOf("") }
    // Estado para los resultados de búsqueda
    val books by viewModel.searchResults.observeAsState(emptyList())
    val context = LocalContext.current
    // Estado para alternar entre vista de lista y cuadrícula
    val isListView = remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    // Estado para mostrar el diálogo de filtro
    var showFilterDialog by remember { mutableStateOf(false) }
    // Estado para el idioma seleccionado
    var selectedLanguage by remember { mutableStateOf<String?>(null) }
    // Lista de idiomas
    val languages = listOf("Inglés", "Español", "Francés", "Alemán", "Catalán", "Sin filtro")

    // Composable para mostrar el icono de filtro
    @Composable
    fun FilterIcon(selectedLanguage: String?) {
        val iconResId = IconResource.languageIcons[selectedLanguage ?: "No Filter"] ?: R.drawable.piplup

        Image(
            painter = painterResource(id = iconResId),
            contentDescription = "Filter",
            modifier = Modifier.size(24.dp)
        )
    }

    // Función para buscar libros
    fun searchBooks() {
        keyboardController?.hide() // Ocultar el teclado
        val languageFilter = when (selectedLanguage) {
            "Inglés" -> "en"
            "Español" -> "es"
            "Francés" -> "fr"
            "Alemán" -> "de"
            "Catalán" -> "ca"
            else -> ""
        }

        // Llamada a la API para buscar libros
        RetrofitInstance.api.searchBooks(query.value, languageFilter, "").enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { bookResponse ->
                        viewModel.searchResults.value = bookResponse.items.map { item ->
                            val coverUrl = item.volumeInfo.imageLinks?.thumbnail ?: "file:///android_asset/predeterminada.jpg"
                            Book(
                                id = item.volumeInfo.id ?: "Unknown ID",
                                title = item.volumeInfo.title,
                                author = item.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author",
                                description = item.volumeInfo.description ?: "No description available",
                                publisher = item.volumeInfo.publisher ?: "Unknown Publisher",
                                coverUrl = coverUrl,
                                notes = "No notes available"
                            )
                        }.sortedWith(compareBy({ it.author }, { it.title }))
                    }
                } else {
                    Toast.makeText(context, "Error en la respuesta: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                Toast.makeText(context, "Error fetching books", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Busqueda de libros") },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        FilterIcon(selectedLanguage)
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Campo de texto para la búsqueda
                OutlinedTextField(
                    value = query.value,
                    onValueChange = { query.value = it },
                    label = { Text("Título, Autor...") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { searchBooks() }
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = { searchBooks() }) {
                        Text("Buscar")
                    }
                    Button(onClick = { isListView.value = !isListView.value }) {
                        Text(if (isListView.value) "Lista" else "Cuadricula")
                    }
                }
                // Mostrar resultados en vista de lista o cuadrícula
                if (isListView.value) {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(books) { book ->
                            val isBookAdded = viewModel.addedBooks.value?.any { it.id == book.id } ?: false
                            BookItemList(book = book, navController = navController, isBookAdded = isBookAdded)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(books) { book ->
                            val isBookAdded = viewModel.addedBooks.value?.any { it.id == book.id } ?: false
                            BookItemGrid(book = book, navController = navController, isBookAdded = isBookAdded)
                        }
                    }
                }
            }
        }
    )

    // Diálogo de filtro
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filtro") },
            text = {
                Column {
                    languages.forEach { language ->
                        LanguageRow(
                            language = language,
                            selectedLanguage = selectedLanguage,
                            onSelect = { selectedLanguage = it }
                        )
                    }
                }
            },
            confirmButton = {
                Row {
                    Button(onClick = { showFilterDialog = false }) {
                        Text("Close")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { searchBooks(); showFilterDialog = false }) {
                        Text("Save")
                    }
                }
            }
        )
    }
}

// Composable para una fila de idioma en el diálogo de filtro
@Composable
fun LanguageRow(language: String, selectedLanguage: String?, onSelect: (String?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onSelect(if (language == "No Filter") null else language)
            }
    ) {
        RadioButton(
            selected = selectedLanguage == language || (selectedLanguage == null && language == "No Filter"),
            onClick = {
                onSelect(if (language == "No Filter") null else language)
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = IconResource.languageIcons[language] ?: R.drawable.predeterminada),
            contentDescription = language,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = language)
    }
}

// Composable para un elemento de libro en la vista de lista
@Composable
fun BookItemList(book: Book, navController: NavController, isBookAdded: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val bookJson = Uri.encode(Gson().toJson(book))
                navController.navigate("bookDetail/$bookJson?isBookAdded=$isBookAdded")
            }
            .padding(8.dp)
    ) {
        Image(
            painter = rememberImagePainter(
                data = book.coverUrl,
                builder = {
                    placeholder(R.drawable.predeterminada)
                    error(R.drawable.predeterminada)
                }
            ),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp, 120.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 8.dp)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.publisher,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isBookAdded) {
                Button(onClick = { /* Navegar a la pantalla de notas */ }) {
                    Text("Notas")
                }
                Button(onClick = { /* Lógica para eliminar libro */ }) {
                    Text("Eliminar")
                }
            }
        }
    }
}

// Composable para un elemento de libro en la vista de cuadrícula
@Composable
fun BookItemGrid(book: Book, navController: NavController, isBookAdded: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val bookJson = Uri.encode(Gson().toJson(book))
                navController.navigate("bookDetail/$bookJson?isBookAdded=$isBookAdded")
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberImagePainter(
                data = book.coverUrl,
                builder = {
                    placeholder(R.drawable.predeterminada)
                    error(R.drawable.predeterminada)
                }
            ),
            contentDescription = null,
            modifier = Modifier
                .size(96.dp, 144.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = book.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isBookAdded) {
            Button(onClick = { /* Navegar a la pantalla de notas */ }) {
                Text("Notas")
            }
            Button(onClick = { /* Lógica para eliminar libro */ }) {
                Text("Eliminar")
            }
        }
    }
}