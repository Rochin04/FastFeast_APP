package ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistroMerchant(
    navController: NavController,
    userId: String, // El ID que recibimos del paso anterior
    viewModel: MerchantSignUpViewModel,
    onRegistrationSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Si el registro es exitoso, navegamos
    LaunchedEffect(uiState.isSuccess, userId) {
        if (uiState.isSuccess) {
            onRegistrationSuccess()
        }
        viewModel.setOwnerId(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos del Comercio (2/2)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState), // Hacemos scrollable por si el teclado tapa
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Configura tu restaurante",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Nombre del Restaurante") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Descripción (Tipo de comida)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.logoUrl,
                onValueChange = { viewModel.onLogoUrlChange(it) },
                label = { Text("Logo de la Empresa") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.locationLatitude,
                onValueChange = { viewModel.onLocationLatitudeChange(it) },
                label = { Text("Latitud") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.locationLongitude,
                onValueChange = { viewModel.onLocationLongitudeChange(it) },
                label = { Text("Longitud") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.address,
                onValueChange = { viewModel.onAddressChange(it) },
                label = { Text("Dirección completa") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Horarios (Simplificados como texto para este ejemplo)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedTextField(
                    value = uiState.openingTime,
                    onValueChange = { viewModel.onOpeningTimeChange(it) },
                    label = { Text("Apertura (HH:MM:SS)") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.closingTime,
                    onValueChange = { viewModel.onClosingTimeChange(it) },
                    label = { Text("Cierre (HH:MM:SS)") },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.error != null) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.registrarComerciante(userId) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Finalizar Registro")
                }
            }
        }
    }
}
