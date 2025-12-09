package ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearPlatillo(
    viewModel: CreateDishViewModel,
    merchantId: String,
    onBack: () -> Unit,
    onDishCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Observamos los campos del VM
    val nombre by viewModel.nombre.collectAsStateWithLifecycle()
    val descripcion by viewModel.descripcion.collectAsStateWithLifecycle()
    val precio by viewModel.precio.collectAsStateWithLifecycle()
    val imagenUrl by viewModel.imagenUrl.collectAsStateWithLifecycle()
    val categoriaNombre by viewModel.categoriaNombre.collectAsStateWithLifecycle()
    val categoriaId by viewModel.categoriaId.collectAsStateWithLifecycle()

    // Snackbars para errores
    val snackbarHostState = remember { SnackbarHostState() }

    // Efecto para manejar el éxito o error
    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateDishUiState.Success -> {
                onDishCreated()
                viewModel.resetState()
            }
            is CreateDishUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as CreateDishUiState.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Crear Nuevo Platillo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { viewModel.nombre.value = it },
                label = { Text("Nombre del platillo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { viewModel.descripcion.value = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = precio,
                onValueChange = { viewModel.precio.value = it },
                label = { Text("Precio ($)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = imagenUrl,
                onValueChange = { viewModel.imagenUrl.value = it },
                label = { Text("URL de la imagen") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://ejemplo.com/foto.jpg") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            val listaCategorias by viewModel.listaCategorias.collectAsStateWithLifecycle()
            val catNombre by viewModel.categoriaNombre.collectAsStateWithLifecycle()

            // Estado para controlar si el menú está expandido o no
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = catNombre,
                    onValueChange = {}, // No dejamos escribir manualmente, solo seleccionar
                    readOnly = true,    // Solo lectura
                    label = { Text("Selecciona una Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor() // Necesario para anclar el menú
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (listaCategorias.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Cargando categorías...") },
                            onClick = { }
                        )
                    } else {
                        listaCategorias.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(text = categoria.nombre) },
                                onClick = {
                                    // Al hacer clic, llenamos automáticamente Nombre e ID en el VM
                                    viewModel.seleccionarCategoria(categoria)
                                    expanded = false // Cerramos el menú
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState is CreateDishUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (merchantId.isNotEmpty()) {
                            viewModel.crearPlatillo(merchantId)
                        } else {
                            // Manejar caso de error si no hay ID (ej. reenviar a login)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Guardar Platillo")
                }

            }
        }
    }
}