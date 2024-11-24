package com.mikel.gestionlibrosv2

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mikel.gestionlibrosv2.ui.theme.GestionLibrosV2Theme
import java.io.File
import java.util.concurrent.Executors

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookNotesScreen(navController: NavController, book: Book, viewModel: BookSearchViewModel) {
    GestionLibrosV2Theme {
        var notes by remember { mutableStateOf(book.notes?.split("\n\n") ?: emptyList()) }
        var showDialog by remember { mutableStateOf(false) }
        var showNoteInputDialog by remember { mutableStateOf(false) }
        var newNote by remember { mutableStateOf("") }
        var page by remember { mutableStateOf("") }
        var showCamera by remember { mutableStateOf(false) }
        var scannedText by remember { mutableStateOf("") }
        var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
        var selectedText by remember { mutableStateOf("") }
        var showSelectedTextDialog by remember { mutableStateOf(false) }
        var showScannedTextDialog by remember { mutableStateOf(false) }
        var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
        var noteToDelete by remember { mutableStateOf("") }
        var expandedNote by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current
        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                showCamera = true
            } else {
                Toast.makeText(context, "Camera permission is required to scan text", Toast.LENGTH_SHORT).show()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Notas de ${book.title}") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            content = { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                    ) {
                        if (notes.isEmpty()) {
                            Text(
                                text = "Todavía no hay notas",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            LazyColumn {
                                items(notes) { note ->
                                    NoteItem(
                                        note = note,
                                        expandedNote = expandedNote,
                                        onExpandNote = { expandedNote = if (expandedNote == note) null else note },
                                        onDeleteNote = {
                                            noteToDelete = note
                                            showDeleteConfirmationDialog = true
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showDialog = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66119))
                        ) {
                            Text("Añadir nota")
                        }
                        if (showDialog) {
                            AddNoteDialog(
                                onDismiss = { showDialog = false },
                                onWriteNote = {
                                    showDialog = false
                                    showNoteInputDialog = true
                                },
                                onScanText = {
                                    showDialog = false
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            )
                        }
                        if (showNoteInputDialog) {
                            NoteInputDialog(
                                newNote = newNote,
                                onNewNoteChange = { newNote = it },
                                page = page,
                                onPageChange = { page = it },
                                onDismiss = { showNoteInputDialog = false },
                                onAddNote = {
                                    if (newNote.isNotEmpty() && page.isNotEmpty()) {
                                        notes = notes + "$page: $newNote"
                                        viewModel.updateBookNotes(book.copy(notes = notes.joinToString("\n\n")))
                                    }
                                    showNoteInputDialog = false
                                }
                            )
                        }
                    }
                    if (showCamera) {
                        CameraPreview(
                            onTextScanned = { scannedText = it },
                            onImageCaptured = { uri -> capturedImageUri = uri }
                        )
                    }
                    if (capturedImageUri != null) {
                        Image(
                            painter = rememberImagePainter(capturedImageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (scannedText.isNotEmpty()) {
                        showScannedTextDialog = true
                    }
                    if (showScannedTextDialog) {
                        ScannedTextDialog(
                            scannedText = scannedText,
                            onDismiss = { showScannedTextDialog = false },
                            onSelectText = { selectedText = it; showSelectedTextDialog = true }
                        )
                    }
                    if (showSelectedTextDialog) {
                        SelectedTextDialog(
                            selectedText = selectedText,
                            page = page,
                            onPageChange = { page = it },
                            onDismiss = { showSelectedTextDialog = false },
                            onSaveNote = {
                                if (selectedText.isNotEmpty() && page.isNotEmpty()) {
                                    notes = notes + "$page: $selectedText"
                                    viewModel.updateBookNotes(book.copy(notes = notes.joinToString("\n\n")))
                                    Toast.makeText(context, "Nota guardada", Toast.LENGTH_SHORT).show()
                                }
                                showSelectedTextDialog = false
                                showScannedTextDialog = false
                                scannedText = ""
                                showCamera = false
                                capturedImageUri = null
                            }
                        )
                    }
                    if (showDeleteConfirmationDialog) {
                        DeleteConfirmationDialog(
                            onDismiss = { showDeleteConfirmationDialog = false },
                            onDelete = {
                                notes = notes.filter { it != noteToDelete }
                                viewModel.updateBookNotes(book.copy(notes = notes.joinToString("\n\n")))
                                showDeleteConfirmationDialog = false
                            }
                        )
                    }
                }
            }
        )
    }
}


@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onWriteNote: () -> Unit,
    onScanText: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Añadir nota") },
        text = {
            Column {
                Button(
                    onClick = { onWriteNote() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66119))
                ) {
                    Text("Escribir nota")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onScanText() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66119))
                ) {
                    Text("Escanear texto")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun NoteItem(
    note: String,
    expandedNote: String?,
    onExpandNote: () -> Unit,
    onDeleteNote: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val displayText = note.substringAfter(":").trim()
            val pageText = note.substringBefore(":").trim()
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = buildAnnotatedString {
                        append(displayText)
                        append("\n")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append("Página $pageText")
                        }
                    },
                    maxLines = if (expandedNote == note) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpandNote() }
                )
            }
        }
        IconButton(onClick = { onDeleteNote() }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Note")
        }
    }
}

@Composable
fun NoteInputDialog(
    newNote: String,
    onNewNoteChange: (String) -> Unit,
    page: String,
    onPageChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddNote: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Escribir nota") },
        text = {
            Column {
                TextField(
                    value = newNote,
                    onValueChange = { onNewNoteChange(it) },
                    label = { Text("Nota") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = page,
                    onValueChange = { onPageChange(it) },
                    label = { Text("Página") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddNote() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66119))
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ScannedTextDialog(
    scannedText: String,
    onDismiss: () -> Unit,
    onSelectText: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Texto Escaneado") },
        text = {
            var textFieldValue by remember { mutableStateOf(TextFieldValue(scannedText)) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SelectionContainer {
                    TextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onSelectText(
                            textFieldValue.text.substring(
                                textFieldValue.selection.start,
                                textFieldValue.selection.end
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66119))
                ) {
                    Text("Seleccionar texto")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun SelectedTextDialog(
    selectedText: String,
    page: String,
    onPageChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSaveNote: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Texto Seleccionado") },
        text = {
            Column {
                Text(selectedText)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = page,
                    onValueChange = { onPageChange(it) },
                    label = { Text("Página") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveNote() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66119))
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Confirmar eliminación") },
        text = { Text("¿Estás seguro de que quieres eliminar esta nota?") },
        confirmButton = {
            Button(
                onClick = { onDelete() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE66119))
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(onTextScanned: (String) -> Unit, onImageCaptured: (Uri) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val executor = Executors.newSingleThreadExecutor()
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    DisposableEffect(lifecycleOwner) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            Log.d("CameraPreview", "Camera bound to lifecycle")
        } catch (e: Exception) {
            Log.e("CameraPreview", "Error binding camera to lifecycle", e)
            Toast.makeText(context, "Failed to bind camera. Please try again.", Toast.LENGTH_SHORT).show()
        }

        onDispose {
            cameraProvider.unbindAll()
            executor.shutdown()
            Log.d("CameraPreview", "Camera unbound from lifecycle")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        if (capturedImageUri != null) {
            Image(
                painter = rememberImagePainter(capturedImageUri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = { capturedImageUri = null },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retake Photo")
            }
        } else {
            Button(
                onClick = {
                    val photoFile = File(context.externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    imageCapture?.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val savedUri = Uri.fromFile(photoFile)
                                capturedImageUri = savedUri
                                val image = InputImage.fromFilePath(context, savedUri)
                                textRecognizer.process(image)
                                    .addOnSuccessListener { visionText ->
                                        Log.d("CameraPreview", "Text scanned successfully: ${visionText.text}")
                                        onTextScanned(visionText.text)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("CameraPreview", "Text scanning failed", e)
                                    }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraPreview", "Photo capture failed: ${exception.message}", exception)
                            }
                        }
                    )
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            ) {
                Text("Capture")
            }
        }
    }
}