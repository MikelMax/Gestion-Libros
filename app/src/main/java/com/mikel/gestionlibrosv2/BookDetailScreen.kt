package com.mikel.gestionlibrosv2

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter

@Composable
fun BookDetailScreen(navController: NavController, book: Book, viewModel: BookSearchViewModel, isBookAdded: Boolean) {
    // Detecta si el tema del sistema es oscuro
    val isDarkTheme = isSystemInDarkTheme()
    // Define el color de la flecha de retroceso basado en el tema
    val arrowColor = if (isDarkTheme) Color.White else Color.Black
    // Define el color del texto basado en el esquema de colores del tema
    val textColor = MaterialTheme.colorScheme.onBackground
    // Estado para mostrar mensajes de Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    // Observa el estado de si el libro ha sido añadido
    val bookAdded by viewModel.bookAdded.observeAsState(false)
    // Observa el estado de si el libro ya ha sido añadido
    val bookAlreadyAdded by viewModel.bookAlreadyAdded.observeAsState(false)
    // Estado para alternar entre diseños
    var isAlternateLayout by remember { mutableStateOf(false) }
    // Estado para mostrar el diálogo de confirmación de eliminación
    var showDialog = remember { mutableStateOf(false) }
    // Estado para mostrar u ocultar la descripción del libro
    var showDescription by remember { mutableStateOf(false) }

    // Efecto lanzado cuando  cambia el estado de bookAdded o bookAlreadyAdded
    LaunchedEffect(bookAdded, bookAlreadyAdded) {
        if (bookAlreadyAdded) {
            snackbarHostState.showSnackbar("Ya lo habías añadido")
            viewModel.bookAlreadyAdded.value = false // Resetea el estado
        } else if (bookAdded) {
            snackbarHostState.showSnackbar("Libro añadido")
            viewModel.bookAdded.value = false // Resetea el estado
        }
    }

    // Estrucutra principal de la pantalla
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Barra superior con botones de retroceso y cambio de diseño
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = arrowColor
                    )
                }
                IconButton(onClick = { isAlternateLayout = !isAlternateLayout }) {
                    Icon(
                        imageVector = Icons.Rounded.SwapHoriz,
                        contentDescription = "Toggle Layout",
                        tint = arrowColor
                    )
                }
            }
        },
        content = { paddingValues ->
            // Contenido principal de la pantalla
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isAlternateLayout) {
                    // Muestra el diseño alternativo si isAlternateLayout es verdadero
                    AlternateLayout(book, textColor, isBookAdded, viewModel, navController, showDialog, showDescription, onDescriptionClick = { showDescription = !showDescription })
                } else {
                    // Muestra el diseño predeterminado si isAlternateLayout es falso
                    DefaultLayout(book, textColor, isBookAdded, viewModel, navController, showDialog, showDescription, onDescriptionClick = { showDescription = !showDescription })
                }
            }
        }
    )

    // Muestra un dialogo de confirmación de eliminación si showDialog es verdadero
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = "Confirmar eliminación") },
            text = { Text(text = "¿Estás seguro de que quieres eliminar este libro?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        viewModel.deleteBook(book, navController)
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog.value = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DefaultLayout(
    book: Book,
    textColor: Color,
    isBookAdded: Boolean,
    viewModel: BookSearchViewModel,
    navController: NavController,
    showDialog: MutableState<Boolean>,
    showDescription: Boolean,
    onDescriptionClick: () -> Unit
) {
    // Columna principal que contiene todos los elementod de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Imagen de la portada del libro
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
                .width(192.dp)
                .height(288.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Titulo del libro
        Text(
            text = book.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        // Autor del libro
        Text(
            text = book.author,
            fontSize = 16.sp,
            color = textColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Etiqueta de la editorial
        Text(
            text = "Editorial",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        // Editorial del libro
        Text(
            text = book.publisher,
            fontSize = 16.sp,
            color = textColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Etiqueta de la descripción
        Text(
            text = "Descripción",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onDescriptionClick() }
        )
        // Descripción del libro (visible si showDescription es verdadero)
        if (showDescription) {
            Text(
                text = book.description,
                fontSize = 16.sp,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Botones de acción (Notas y Eliminar) si el libro ya está añadido
        if (isBookAdded) {
            Button(
                onClick = { navController.navigate("book_notes/${book.id}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66119))
            ) {
                Text(
                    text = "Notas",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = { showDialog.value = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(
                    text = "Eliminar libro",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Botón para añadir el libro si no está añadido
            Button(
                onClick = { viewModel.addBook(book) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66119))
            ) {
                Text(
                    text = "Añadir libro",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AlternateLayout(
    book: Book,
    textColor: Color,
    isBookAdded: Boolean,
    viewModel: BookSearchViewModel,
    navController: NavController,
    showDialog: MutableState<Boolean>,
    showDescription: Boolean,
    onDescriptionClick: () -> Unit
) {
    // Columna principal que contiene todos los elementos de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Fila que contiene la imagen y la información del libro
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de la portada del libro
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
                    .width(192.dp)
                    .height(288.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Columna que contiene la información del libro
            Column {
                // Etiqueta del título
                Text(
                    text = "Título",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                // Título del libro
                Text(
                    text = book.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                // Etiqueta del autor
                Text(
                    text = "Autor",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                // Autor del libro
                Text(
                    text = book.author,
                    fontSize = 16.sp,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                // Etiqueta de la editorial
                Text(
                    text = "Editorial",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                // Editorial del libro
                Text(
                    text = book.publisher,
                    fontSize = 16.sp,
                    color = textColor
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Etiqueta de la descripción
        Text(
            text = "Descripción",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onDescriptionClick() }
        )
        // Descripción del libro (visible si showDescription es verdadero)
        if (showDescription) {
            Text(
                text = book.description,
                fontSize = 14.sp,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Botones de acción (Notas y Eliminar) si el libro ya está añadido
        if (isBookAdded) {
            Button(
                onClick = { navController.navigate("book_notes/${book.id}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66119))
            ) {
                Text(
                    text = "Notas",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = { showDialog.value = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(
                    text = "Eliminar libro",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Botón para añadir el libro si no está añadido
            Button(
                onClick = { viewModel.addBook(book) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66119))
            ) {
                Text(
                    text = "Añadir libro",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookDetailScreenPreview() {
    // Crea un objeto Book de ejemplo
    val book = Book(
        id = "1",
        title = "El Imperio Final",
        author = "Brandon Sanderson",
        description = "Nacidos de la bruma: El imperio final tiene lugar en un equivalente a principios del siglo XVIII, en el distópico mundo de Scadrial, donde cae ceniza constantemente del cielo, las plantas son color café, y brumas sobrenaturales cubren el paisaje cada noche. Mil años antes del inicio de la novela, el profetizado Héroe de las Eras ascendió a la divinidad en el Pozo de la ascensión para repeler la Profundidad, un terror que acecha el mundo, cuya naturaleza real se ha perdido con el tiempo. Aunque la Profundidad fue exitosamente repelida y la humanidad se salvó, el mundo fue cambiado a su forma actual por el Héroe, quién tomó el título Lord Legislador y ha gobernado sobre el Imperio Final por mil años como un tirano inmortal y dios. Bajo su reinado, la sociedad fue estratificada en la nobleza, que se cree que fueron los descendientes de los amigos y aliados que le ayudaron a conseguir la divinidad, y los skaa, el campesinado brutalmente oprimido que desciende de aquellos que se opusieron a él.",
        publisher = "Nova",
        coverUrl = "file:///android_asset/predeterminada.jpg",
        notes = ""
    )
    // Obtiene el contexto actual
    val context = LocalContext.current
    // Crea una instancia de BookSearchViewModel usando el contexto de la aplicación
    val viewModel = BookSearchViewModel(context.applicationContext as Application)
    // Llama a la función BookDetailScreen con los parámetros necesarios
    BookDetailScreen(navController = NavController(context), book = book, viewModel = viewModel, isBookAdded = false)
}