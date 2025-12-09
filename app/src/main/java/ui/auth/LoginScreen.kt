package ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin(
    authViewModel: LoginViewModel,
    onGoToRegister: (String) -> Unit,
    // Nuevo parámetro: Callback cuando el login es exitoso
    // Recibe el tipo de usuario (String) para saber a dónde ir
    onLoginSuccess: (String) -> Unit
) {
    // Estado local
    var selectedUserType by remember { mutableStateOf("merchant") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Estado del ViewModel
    val uiState by authViewModel.uiState.collectAsState()

    // --- LÓGICA DE ÉXITO (Igual que en Registro Student) ---
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            // Pasamos el tipo de usuario (o "student" por defecto si es nulo)
            onLoginSuccess(uiState.userType ?: "student")
            // Opcional: Resetear el estado para evitar rebotes si vuelves atrás
            // authViewModel.resetState()
        }
    }
    // -------------------------------------------------------

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¿Cómo deseas ingresar?", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedUserType == "merchant",
                onClick = { selectedUserType = "merchant" }
            )
            Text("Comerciante")

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = selectedUserType == "student",
                onClick = { selectedUserType = "student" }
            )
            Text("Estudiante")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { authViewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Iniciar Sesión")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { onGoToRegister(selectedUserType) }) {
            Text("Registrarse como ${if(selectedUserType == "merchant") "Comerciante" else "Estudiante"}")
        }
    }
}