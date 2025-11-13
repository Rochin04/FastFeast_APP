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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import ui.main.MainUiState
import ui.main.MainViewModel
import com.example.fastfeastv01.ui.theme.FastFeastTheme
import java.net.URLDecoder

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

    @OptIn(ExperimentalMaterial3Api::class) // Necesario para TopAppBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FastFeastTheme {
                val navController = rememberNavController()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                NavHost(navController = navController, startDestination = AppScreen.Principal.route) {

                    composable(route = AppScreen.Principal.route) {
                        Scaffold(containerColor = Color(0xFFF5F5F5)) { innerPadding ->
                            if (uiState.isLoading) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                PantallaPrincipal(
                                    uiState = uiState,
                                    modifier = Modifier.padding(innerPadding),
                                    onPlatilloClick = { platillo ->
                                        navController.navigate(AppScreen.Detalles.createRoute(platillo))
                                    }
                                )
                            }
                        }
                    }

                    composable(
                        route = AppScreen.Detalles.route,
                        arguments = listOf(
                            navArgument("platilloId") { type = NavType.IntType },
                            navArgument("platilloNombre") { type = NavType.StringType },
                            navArgument("platilloDescripcion") { type = NavType.StringType },
                            navArgument("platilloPrecio") { type = NavType.FloatType },
                            navArgument("platilloImagenUrl") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val arguments = requireNotNull(backStackEntry.arguments)
                        val platillo = Platillo(
                            id = arguments.getInt("platilloId"),
                            nombre = arguments.getString("platilloNombre") ?: "",
                            descripcion = URLDecoder.decode(arguments.getString("platilloDescripcion") ?: "", "UTF-8"),
                            precio = arguments.getFloat("platilloPrecio").toDouble(),
                            imagenUrl = URLDecoder.decode(arguments.getString("platilloImagenUrl") ?: "", "UTF-8")
                        )

                        // --> CAMBIO: La llamada ahora es mucho más simple. Ya no le pasamos las categorías.
                        PantallaDetalles(
                            platillo = platillo,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

    // --- PANTALLA PRINCIPAL ---
    @Composable
    fun PantallaPrincipal(
        uiState: MainUiState,
        modifier: Modifier = Modifier,
        onPlatilloClick: (Platillo) -> Unit
    ) {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            item { CabeceraApp() }
            item { BarraBusqueda() }
            item { SeccionCategorias(categorias = uiState.categorias) }
            item {
                Text(
                    text = "Platillos Populares",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                )
            }
            items(uiState.platillos) { platillo ->
                ItemPlatillo(
                    platillo = platillo,
                    onPlatilloClick = { onPlatilloClick(platillo) }
                )
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
                Text(
                    text = "¡Hola!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "¿Qué te apetece hoy?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
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
                    ItemCategoria(
                        categoria = categoria,
                        onCategoriaClick = { /* Lógica de clic */ })
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
            Text(
                text = categoria.nombre,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }

    @Composable
    fun ItemPlatillo(
        platillo: Platillo,
        onPlatilloClick: () -> Unit
    ) { // La firma cambia ligeramente
        Card(
            // ... (resto de tus modificadores sin cambios)
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { onPlatilloClick() }  // Llama a la lambda recibida
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
                    Text(
                        text = platillo.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = platillo.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2
                    )
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

@OptIn(ExperimentalMaterial3Api::class) // Necesario para TopAppBar
@Composable
fun PantallaDetalles(
    platillo: Platillo,
    navController: NavController
) {
    Scaffold(
        // --> CAMBIO: Usamos una TopAppBar en lugar de la cabecera genérica
        topBar = {
            TopAppBar(
                title = { Text(platillo.nombre) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Acción para volver
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F5) // Mismo color de fondo
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        // --> CAMBIO: El contenido es un LazyColumn que solo muestra el detalle
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplicamos el padding de la TopAppBar
        ) {
            item {
                DetallePlatillo(platillo = platillo)
            }
        }
    }
}

@Composable
fun DetallePlatillo(platillo: Platillo) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        // Imagen del platillo
        AsyncImage(
            model = platillo.imagenUrl,
            contentDescription = "Imagen de ${platillo.nombre}",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_launcher_background)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Contenedor para el resto de la información
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Nombre del platillo
            Text(
                text = platillo.nombre,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Descripción
            Text(
                text = platillo.descripcion,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Precio
            Text(
                text = "$${String.format("%.2f", platillo.precio)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Botón de acción
            Button(
                onClick = { /* Lógica para añadir al carrito */ },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Añadir al Carrito", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp)) // Espacio extra al final
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
            // La preview no necesita el controlador de navegación
            PantallaPrincipal(uiState = previewState, onPlatilloClick = {})
        }
    }

