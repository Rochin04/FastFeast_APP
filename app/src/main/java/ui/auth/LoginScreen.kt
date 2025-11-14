package ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PantallaLogin(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Debes iniciar sesión para continuar")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                authViewModel.login()
                // Una vez logueado, volvemos a la pantalla anterior
                navController.popBackStack()
            }) {
                Text("Simular Inicio de Sesión")
            }
        }
    }
}
