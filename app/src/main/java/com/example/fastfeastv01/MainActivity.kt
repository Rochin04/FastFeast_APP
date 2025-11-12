package com.example.fastfeastv01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ui.main.MainUiState
import ui.main.MainViewModel
import com.example.fastfeastv01.ui.theme.FastFeastTheme

// --- MODELOS DE DATOS ---
data class Platillo(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val imagenUrl: String
)

data class Categoria(
    val nombre: String,
    @DrawableRes val icono: Int
)

// --- ACTIVIDAD PRINCIPAL ---
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FastFeastTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(containerColor = Color(0xFFF5F5F5)) { innerPadding ->
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // Pasamos el padding directamente a la LazyColumn
                        PantallaPrincipal(
                            uiState = uiState,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

// --- PANTALLA PRINCIPAL ---
@Composable
fun PantallaPrincipal(uiState: MainUiState, modifier: Modifier = Modifier) {
    // LazyColumn es el contenedor principal y único que se desplaza verticalmente.
    LazyColumn(
        modifier = modifier.fillMaxSize() // El modifier ya incluye el padding del Scaffold
    ) {
        // Cada 'item' es un bloque de contenido estático
        item {
            CabeceraApp()
        }
        item {
            BarraBusqueda()
        }
        item {
            SeccionCategorias(categorias = uiState.categorias)
        }
        item {
            Text(
                text = "Platillos Populares",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
            )
        }

        // 'items' toma la lista de platillos y crea un elemento por cada uno.
        // ESTA es la forma correcta de renderizar una lista dinámica dentro de LazyColumn.
        items(uiState.platillos) { platillo ->
            ItemPlatillo(platillo = platillo, onPlatilloClick = {})
        }
    }
}

// --- COMPONENTES INDIVIDUALES (¡ESTA ES LA PARTE QUE FALTABA!) ---

@Composable
fun CabeceraApp() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "¡Hola!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Text(text = "¿Qué te apetece hoy?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background), // Reemplaza con tu logo
            contentDescription = "Avatar de usuario",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraBusqueda() {
    var textoBusqueda by remember { mutableStateOf("") }
    OutlinedTextField(
        value = textoBusqueda,
        onValueChange = { textoBusqueda = it },
        placeholder = { Text("Busca tu platillo favorito...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ícono de búsqueda") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(30.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Composable
fun SeccionCategorias(categorias: List<Categoria>) {
    Column { // El Column que tenías estaba bien, pero sin el padding vertical que limitaba.
        Text(
            text = "Menús",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categorias) { categoria ->
                ItemCategoria(categoria = categoria, onCategoriaClick = { /* Lógica de clic */ })
            }
        }
    }
}

@Composable
fun ItemCategoria(categoria: Categoria, onCategoriaClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onCategoriaClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = categoria.icono),
                contentDescription = categoria.nombre,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = categoria.nombre, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ItemPlatillo(platillo: Platillo, onPlatilloClick: (Platillo) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onPlatilloClick(platillo) }
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = platillo.imagenUrl,
                contentDescription = "Imagen de ${platillo.nombre}",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_launcher_background)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = platillo.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = platillo.descripcion, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${String.format("%.2f", platillo.precio)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


// --- VISTA PREVIA ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PantallaPrincipalPreview() {
    FastFeastTheme {
        val previewState = MainUiState(
            categorias = listOf(
                Categoria("Hamburguesas", R.drawable.ic_launcher_foreground),
                Categoria("Pizza", R.drawable.ic_launcher_foreground)
            ),
            platillos = listOf(
                Platillo(1, "Hamburguesa Clásica", "Carne de res, lechuga...", 10.99, "")
            ),
            isLoading = false
        )
        PantallaPrincipal(uiState = previewState)
    }
}

