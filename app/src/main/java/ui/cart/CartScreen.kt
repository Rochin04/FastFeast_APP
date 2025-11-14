package ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fastfeastv01.Platillo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCarrito(
    navController: NavController,
    cartViewModel: CartViewModel = viewModel() // Obtiene la instancia del ViewModel
) {
    val uiState by cartViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (uiState.platillos.isEmpty()) {
                Text("Tu carrito está vacío.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.platillos) { platillo ->
                        ItemCarrito(platillo)
                        Divider()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Total: $${String.format("%.2f", uiState.total)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ItemCarrito(platillo: Platillo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(platillo.nombre, style = MaterialTheme.typography.bodyLarge)
        Text(
            "$${String.format("%.2f", platillo.precio)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}