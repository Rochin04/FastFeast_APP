package com.example.fastfeastv01

import android.annotation.SuppressLint
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
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.viewmodel.compose.viewModel
import ui.cart.CartViewModel
import ui.cart.PantallaCarrito
import java.nio.charset.StandardCharsets
import data.UserPreferencesRepository
import ui.auth.AuthViewModel
import ui.auth.AuthViewModelFactory
import ui.auth.PantallaLogin
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch

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
    // ViewModel para la pantalla principal
    private val viewModel: MainViewModel by viewModels()
    // ViewModel compartido para el carrito
    private val cartViewModel: CartViewModel by viewModels()

    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(userPreferencesRepository)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferencesRepository = UserPreferencesRepository(applicationContext)
        enableEdgeToEdge()
        setContent {
            FastFeastTheme {
                val navController = rememberNavController()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                // --- NUEVO: Contenedor principal que permite un drawer ---
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        // --- NUEVO: Contenido del menú lateral ---
                        AppDrawer(
                            navController = navController,
                            closeDrawer = { scope.launch { drawerState.close() } }
                        )
                    }
                ) {
                    // --- Grafo de Navegación (Ahora dentro del ModalNavigationDrawer) ---
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
                                        },
                                        authViewModel = authViewModel,
                                        navController = navController,
                                        // --- NUEVO: Pasamos la acción para abrir el drawer ---
                                        onMenuClick = {
                                            scope.launch { drawerState.open() }
                                        }
                                    )
                                }
                            }
                        }

                        // Ruta a Detalles (sin cambios)
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
                                descripcion = URLDecoder.decode(arguments.getString("platilloDescripcion") ?: "", StandardCharsets.UTF_8.toString()),
                                precio = arguments.getFloat("platilloPrecio").toDouble(),
                                imagenUrl = URLDecoder.decode(arguments.getString("platilloImagenUrl") ?: "", StandardCharsets.UTF_8.toString())
                            )
                            PantallaDetalles(
                                platillo = platillo,
                                navController = navController,
                                cartViewModel = cartViewModel,
                                authViewModel = authViewModel
                            )
                        }
                        // Ruta Login (sin cambios)
                        composable(route = AppScreen.Login.route) {
                            PantallaLogin(
                                navController = navController,
                                authViewModel = authViewModel
                            )
                        }
                        // Ruta Carrito (sin cambios)
                        composable(route = AppScreen.Carrito.route) {
                            PantallaCarrito(
                                navController = navController,
                                cartViewModel = cartViewModel
                            )
                        }

                        // --- NUEVO: Rutas para las nuevas pantallas del drawer ---
                        composable(route = AppScreen.Perfil.route) {
                            PantallaPerfil(navController = navController)
                        }

                        composable(route = AppScreen.Configuracion.route) {
                            PantallaConfiguracion(navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppDrawer(
    navController: NavHostController,
    closeDrawer: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalDrawerSheet {
        // Un encabezado simple para el drawer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "FastFeast",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Items de navegación
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Menu, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = currentRoute == AppScreen.Principal.route,
            onClick = {
                navController.navigate(AppScreen.Principal.route) {
                    popUpTo(AppScreen.Principal.route) { inclusive = true }
                }
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Mi Perfil") },
            selected = currentRoute == AppScreen.Perfil.route,
            onClick = {
                navController.navigate(AppScreen.Perfil.route)
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") },
            label = { Text("Configuración") },
            selected = currentRoute == AppScreen.Configuracion.route,
            onClick = {
                navController.navigate(AppScreen.Configuracion.route)
                closeDrawer()
            }
        )
    }
}

// --- NUEVO: Pantallas de ejemplo para Perfil y Configuración ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Aquí va la información del perfil del usuario.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracion(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("Aquí van las opciones de configuración.")
        }
    }
}

    // --- PANTALLA PRINCIPAL ---
    @Composable
    fun PantallaPrincipal(
        uiState: MainUiState,
        modifier: Modifier = Modifier,
        onPlatilloClick: (Platillo) -> Unit,
        authViewModel: AuthViewModel,
        navController: NavController,
        onMenuClick: () -> Unit // <-- NUEVO: Recibe la función para abrir el drawer
    ) {
        val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
        LazyColumn(modifier = modifier.fillMaxSize()) {
            item {
                CabeceraApp(
                    isLoggedIn = isLoggedIn,
                    onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate(AppScreen.Principal.route) {
                            popUpTo(AppScreen.Principal.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onMenuClick = onMenuClick // <-- MODIFICADO: Pasa la acción a la cabecera
                )
            }
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
    fun CabeceraApp(
        isLoggedIn: Boolean,
        onLogoutClick: () -> Unit,
        onMenuClick: () -> Unit // <-- NUEVO: Recibe la acción del menú
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- MODIFICADO: Se añade el ícono de menú ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) { // <-- NUEVO
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Abrir menú"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
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
            }

            if (isLoggedIn) {
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo de la app",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalles(
    platillo: Platillo,
    navController: NavController,
    cartViewModel: CartViewModel, // <-- 1. ACEPTA EL VIEWMODEL AQUÍ
    authViewModel: AuthViewModel // <-- RECIBE EL VIEWMODEL DE AUTH
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(platillo.nombre) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                // 2. PASA LA ACCIÓN AL COMPOSABLE HIJO
                DetallePlatillo(
                    platillo = platillo,
                    onAddToCart = {
                        if (isLoggedIn) {
                            // Si está logueado, añade al carrito
                            cartViewModel.agregarAlCarrito(platillo)
                            navController.navigate(AppScreen.Carrito.route)
                        } else {
                            // Si no, navega a la pantalla de login
                            navController.navigate(AppScreen.Login.route)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DetallePlatillo(
    platillo: Platillo,
    onAddToCart: () -> Unit // <-- 1. ACEPTA UNA FUNCIÓN LAMBDA
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        // ... (Tu código para AsyncImage, Text del nombre, descripción y precio no cambia) ...

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

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = platillo.nombre,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = platillo.descripcion,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))

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
                onClick = onAddToCart, // <-- 2. EJECUTA LA LAMBDA RECIBIDA
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Añadir al Carrito", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

    // --- VISTA PREVIA ---
    @SuppressLint("ViewModelConstructorInComposable")
    @Preview(showBackground = true, showSystemUi = true)
    @Composable
// Añade esta línea para suprimir el warning específico de la preview
    @Suppress("viewModel_injection_in_preview")
    fun PantallaPrincipalPreview() {
        FastFeastTheme {
            val navController = rememberNavController()
            val context = LocalContext.current

            // Esta línea es la que genera el warning, pero es correcta para la preview.
            // La anotación @Suppress de arriba lo soluciona.
            val authViewModel = AuthViewModel(UserPreferencesRepository(context))

            val previewState = MainUiState(
                categorias = listOf(
                    Categoria("Hamburguesas", R.drawable.ic_launcher_foreground),
                    Categoria("Pizza", R.drawable.ic_launcher_foreground),
                    Categoria("Bebidas", R.drawable.ic_launcher_foreground)
                ),
                platillos = listOf(
                    Platillo(1, "Hamburguesa Clásica", "Carne de res, lechuga...", 10.99, ""),
                    Platillo(2, "Pizza Margarita", "Salsa de tomate, mozzarella...", 12.50, "")
                ),
                isLoading = false
            )

            // --- CORRECCIÓN AQUÍ ---
            PantallaPrincipal(
                uiState = previewState,
                onPlatilloClick = {},
                authViewModel = authViewModel,
                navController = navController,
                onMenuClick = {} // <-- AÑADE ESTA LÍNEA
            )
        }
    }

