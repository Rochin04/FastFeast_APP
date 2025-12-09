package com.example.fastfeastv01

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import com.example.fastfeastv01.data.Platillo
import com.example.fastfeastv01.data.Categoria
import data.NetworkPlatillosRepository
import ui.cart.CartViewModelFactory
import ui.main.MainViewModelFactory
import data.AuthRepository
import ui.auth.LoginViewModel
import ui.auth.MerchantSignUpViewModel
import ui.auth.MerchantSignUpViewModelFactory
import ui.auth.PantallaPerfil
import ui.auth.SignUpViewModel
import ui.auth.SignUpViewModelFactory
import ui.auth.PantallaRegistro
import ui.auth.PantallaRegistroMerchant
import ui.auth.PantallaRegistroStudent
import ui.auth.ProfileViewModel
import ui.auth.StudentSignUpViewModel
import ui.auth.StudentSignUpViewModelFactory
import ui.auth.PantallaCrearPlatillo
import ui.auth.CreateDishViewModel
import ui.auth.CreateDishViewModelFactory

// --- ACTIVIDAD PRINCIPAL ---
class MainActivity : ComponentActivity() {
    // Repositorio de datos para los platillos
    private val repository = NetworkPlatillosRepository()

    // Repositorio de preferencias de usuario
    private val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(applicationContext)
    }
    // 1. ViewModel de autenticación
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(userPreferencesRepository)
    }

    // 2. ViewModel principal
    private val viewModel: MainViewModel by viewModels {
        // Ahora la factory pide (repository, authRepository)
        MainViewModelFactory(repository, authRepository)
    }

    // 3. ViewModel del carrito
    private val cartViewModel: CartViewModel by viewModels {
        CartViewModelFactory(authViewModel)
    }

    private val authRepository: AuthRepository by lazy {
        AuthRepository(userPreferencesRepository)
    }

    // ViewModel de Registro (Paso 1)
    private val signUpViewModel: SignUpViewModel by viewModels {
        SignUpViewModelFactory(authRepository)
    }

    // ViewModel de Registro Merchant (Paso 2)
    private val merchantSignUpViewModel: MerchantSignUpViewModel by viewModels {
        MerchantSignUpViewModelFactory(authRepository)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FastFeastTheme {
                val navController = rememberNavController()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val context = LocalContext.current
                val userPreferencesRepository = UserPreferencesRepository(context)
                val authRepository = AuthRepository(userPreferencesRepository)
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModel.LoginViewModelFactory(authRepository)
                )
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.Factory(authRepository)
                )
                val currentUserId by userPreferencesRepository.userId.collectAsState(initial = "")
                val createDishViewModel: CreateDishViewModel = viewModel(
                    factory = CreateDishViewModelFactory(repository)
                )
                // --- Contenedor principal que permite un drawer ---
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                    AppDrawer(
                            navController = navController,
                            mainViewModel = viewModel,
                            closeDrawer = { scope.launch { drawerState.close() } }
                        )
                    }
                ) {
                    // --- Grafo de Navegación ---
                    NavHost(navController = navController, startDestination = AppScreen.Principal.route) {

                        // 1. PANTALLA PRINCIPAL
                        composable(route = AppScreen.Principal.route) {
                            Scaffold(containerColor = Color(0xFFF5F5F5)) { innerPadding ->
                                if (uiState.isLoading) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    PantallaPrincipal(
                                        uiState = uiState,
                                        modifier = Modifier.padding(innerPadding),
                                        onPlatilloClick = { platillo ->
                                            navController.navigate(
                                                AppScreen.Detalles.createRoute(
                                                    platillo
                                                )
                                            )
                                        },
                                        authViewModel = authViewModel,
                                        navController = navController,
                                        onMenuClick = {
                                            scope.launch { drawerState.open() }
                                        },
                                        onRetry = { viewModel.getPlatillos() },
                                        onLoginClick = {
                                            // CORRECCIÓN IMPORTANTE: Ir a Login, no a Registro directo
                                            navController.navigate(AppScreen.Login.route)
                                        }
                                    )
                                }
                            }
                        }

                        // 2. PANTALLA DETALLES
                        composable(
                            route = AppScreen.Detalles.route,
                            arguments = listOf(
                                navArgument("platilloId") { type = NavType.StringType },
                                navArgument("platilloNombre") { type = NavType.StringType },
                                navArgument("platilloDescripcion") { type = NavType.StringType },
                                navArgument("platilloPrecio") { type = NavType.FloatType },
                                navArgument("platilloImagenUrl") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val arguments = requireNotNull(backStackEntry.arguments)
                            val platillo = Platillo(
                                id = arguments.getString("platilloId") ?: "",
                                nombre = URLDecoder.decode(
                                    arguments.getString("platilloNombre") ?: "",
                                    StandardCharsets.UTF_8.toString()
                                ),
                                descripcion = URLDecoder.decode(
                                    arguments.getString("platilloDescripcion") ?: "",
                                    StandardCharsets.UTF_8.toString()
                                ),
                                precio = arguments.getFloat("platilloPrecio").toDouble(),
                                imagenUrl = URLDecoder.decode(
                                    arguments.getString("platilloImagenUrl") ?: "",
                                    StandardCharsets.UTF_8.toString()
                                )
                            )
                            PantallaDetalles(
                                platillo = platillo,
                                navController = navController,
                                cartViewModel = cartViewModel,
                                authViewModel = authViewModel
                            )
                        }

                        // 3. PANTALLA LOGIN (Solo una vez)
                        composable(route = AppScreen.Login.route) {
                            PantallaLogin(
                                authViewModel = loginViewModel,
                                onGoToRegister = { typeSelected ->
                                    navController.navigate("registro/$typeSelected")
                                },
                                onLoginSuccess = { userType ->
                                    // AQUÍ MANEJAS LA NAVEGACIÓN SEGURA
                                    if (userType == "merchant") {
                                        // ADVERTENCIA: Debes crear esta ruta en tu NavHost si no existe,
                                        // si no tienes pantalla de comerciante aún, mándalo a Principal temporalmente.
                                        // navController.navigate("pantalla_comerciante_home")

                                        // Por ahora, para evitar el error crash, lo mandamos a Principal:
                                        navController.navigate(AppScreen.Principal.route) {
                                            popUpTo(AppScreen.Login.route) { inclusive = true }
                                        }
                                    } else {
                                        // Si es estudiante, va a la pantalla principal (AppScreen.Principal.route)
                                        navController.navigate(AppScreen.Principal.route) {
                                            // Esto borra el login del historial para que 'Atrás' cierre la app
                                            popUpTo(AppScreen.Login.route) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        // 4. PANTALLA CARRITO
                        composable(route = AppScreen.Carrito.route) {
                            PantallaCarrito(
                                navController = navController,
                                cartViewModel = cartViewModel
                            )
                        }

                        // 5. PANTALLAS DEL DRAWER
                        composable(route = AppScreen.Perfil.route) {
                            PantallaPerfil(
                                navController = navController,
                                viewModel = profileViewModel
                            )

                        }

                        composable(route = AppScreen.Configuracion.route) {
                            PantallaConfiguracion(navController = navController)
                        }

                        // 6. REGISTRO PASO 1 (Genérico con argumento type)
                        composable(
                            route = "registro/{userType}",
                            arguments = listOf(navArgument("userType") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val userType =
                                backStackEntry.arguments?.getString("userType") ?: "merchant"

                            PantallaRegistro(
                                navController = navController,
                                viewModel = signUpViewModel,
                                userType = userType,
                                onUserCreated = { userId ->
                                    // Bifurcación según el tipo
                                    if (userType == "student") {
                                        navController.navigate("registro_student/$userId")
                                    } else {
                                        navController.navigate(
                                            AppScreen.RegistroMerchant.createRoute(
                                                userId
                                            )
                                        )
                                    }
                                }
                            )
                        }

                        // 7. REGISTRO COMERCIANTE PASO 2
                        composable(
                            route = AppScreen.RegistroMerchant.route,
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""

                            PantallaRegistroMerchant(
                                navController = navController,
                                userId = userId,
                                viewModel = merchantSignUpViewModel,
                                onRegistrationSuccess = {
                                    navController.navigate(AppScreen.Principal.route) {
                                        popUpTo(AppScreen.Principal.route) { inclusive = false }
                                    }
                                }
                            )
                        }

                        // 8. REGISTRO ESTUDIANTE PASO 2
                        composable(
                            route = "registro_student/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""

                            val studentViewModel: StudentSignUpViewModel = viewModel(
                                factory = StudentSignUpViewModelFactory(
                                    AuthRepository(UserPreferencesRepository(LocalContext.current))
                                )
                            )

                            PantallaRegistroStudent(
                                viewModel = studentViewModel,
                                userId = userId,
                                onSuccess = {
                                    navController.navigate(AppScreen.Principal.route) {
                                        popUpTo(AppScreen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("create_dish") {
                            PantallaCrearPlatillo(
                                // ERROR ANTERIOR: viewModel = CreateDishViewModel (Clase)
                                // CORRECTO: viewModel = createDishViewModel (Variable definida arriba)
                                viewModel = createDishViewModel,

                                // ERROR ANTERIOR: currentUserId no existía
                                // CORRECTO: Usamos la variable que creamos con collectAsState
                                merchantId = currentUserId ?: "",

                                onBack = { navController.popBackStack() },
                                onDishCreated = {
                                    viewModel.getPlatillos()
                                    navController.navigate(AppScreen.Principal.route) {
                                        popUpTo(AppScreen.Principal.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES (Drawer, Perfil, Config, Cabecera, etc.) ---

@Composable
fun AppDrawer(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    closeDrawer: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val userRole by mainViewModel.userRole.collectAsStateWithLifecycle()

    ModalDrawerSheet {
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
//        if (userRole == "student") {
//            NavigationDrawerItem(
//                icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = null) },
//                label = { Text("Ordenar Comida") },
//                selected = currentRoute == "order_food",
//                onClick = {
//                    navController.navigate("order_food")
//                    closeDrawer()
//                }
//            )
//        }

        // 2. Comerciante
        if (userRole == "merchant") {
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                label = { Text("Crear Platillo") },
                selected = currentRoute == "create_dish",
                onClick = {
                    navController.navigate("create_dish")
                    closeDrawer()
                }
            )
        }
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") },
            label = { Text("Configuración") },
            selected = currentRoute == AppScreen.Configuracion.route,
            onClick = {
                navController.navigate(AppScreen.Configuracion.route)
                closeDrawer()
            }
        )
        Spacer(modifier = Modifier.weight(1f))

        // Botón de cerrar sesión
        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
            label = { Text("Cerrar Sesión") },
            selected = false,
            onClick = {
                mainViewModel.logout() // Limpia preferencias
                closeDrawer()
                navController.navigate(AppScreen.Login.route) {
                    popUpTo(0) // Limpia toda la pila de navegación
                }
            }
        )
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

@Composable
fun PantallaPrincipal(
    uiState: MainUiState,
    modifier: Modifier = Modifier,
    onPlatilloClick: (Platillo) -> Unit,
    authViewModel: AuthViewModel,
    navController: NavController,
    onMenuClick: () -> Unit,
    onRetry: () -> Unit,
    onLoginClick: () -> Unit
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
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
                onMenuClick = onMenuClick,
                onLoginClick = onLoginClick
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
        if (uiState.platillos.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No se encontraron platillos disponibles.")
                }
            }
        } else {
            items(uiState.platillos) { platillo ->
                ItemPlatillo(
                    platillo = platillo,
                    onPlatilloClick = { onPlatilloClick(platillo) }
                )
            }
        }
    }
}

@Composable
fun CabeceraApp(
    isLoggedIn: Boolean,
    onLogoutClick: () -> Unit,
    onMenuClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Abrir menú")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (isLoggedIn) "¡Hola!" else "Bienvenido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "¿Qué te apetece?",
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
            Button(
                onClick = onLoginClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Entrar", style = MaterialTheme.typography.labelLarge)
            }
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
    Column {
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
fun ItemPlatillo(platillo: Platillo, onPlatilloClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onPlatilloClick() }
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
                    text = platillo.descripcion ?: "Sin descripción disponible",
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
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(platillo.nombre) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F5))
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            item {
                DetallePlatillo(
                    platillo = platillo,
                    onAddToCart = {
                        if (isLoggedIn) {
                            cartViewModel.agregarAlCarrito(platillo)
                            navController.navigate(AppScreen.Carrito.route)
                        } else {
                            navController.navigate(AppScreen.Login.route)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DetallePlatillo(platillo: Platillo, onAddToCart: () -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
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
                text = platillo.descripcion ?: "Sin descripción disponible",
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

            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Añadir al Carrito", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, showSystemUi = true)
@Composable
@Suppress("viewModel_injection_in_preview")
fun PantallaPrincipalPreview() {
    FastFeastTheme {
        val navController = rememberNavController()
        val context = LocalContext.current
        // 1. Crear el Repo de Preferencias
        val userPrefs = UserPreferencesRepository(context)

        // 2. Crear el AuthRepository usando las preferencias
        val authRepo = AuthRepository(userPrefs)

        // 3. Crear el ViewModel usando el AuthRepository (NO las preferencias directo)
        val authViewModel = AuthViewModel(authRepo)

        val previewState = MainUiState(
            categorias = listOf(
                // AHORA TIENEN 3 ARGUMENTOS: ID, NOMBRE, ICONO
                Categoria("1", "Hamburguesas", R.drawable.ic_launcher_foreground),
                Categoria("2", "Pizza", R.drawable.ic_launcher_foreground),
                Categoria("3", "Bebidas", R.drawable.ic_launcher_foreground)
            ),
            platillos = listOf(
                Platillo("1", "Hamburguesa Clásica", "Carne de res, lechuga...", 10.99, ""),
                Platillo("2", "Pizza Margarita", "Salsa de tomate, mozzarella...", 12.50, "")
            ),
            isLoading = false
        )

        PantallaPrincipal(
            uiState = previewState,
            onPlatilloClick = {},
            authViewModel = authViewModel,
            navController = navController,
            onMenuClick = {},
            onRetry = {},
            onLoginClick = {}
        )
    }
}
