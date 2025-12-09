package ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Asegúrate de importar tu ViewModel de Estudiante
// import com.example.fastfeastv01.ui.auth.StudentSignUpViewModel

@Composable
fun PantallaRegistroStudent(
    viewModel: StudentSignUpViewModel,
    userId: String,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Seteamos el ID del usuario apenas entramos a la pantalla
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    // Si el registro es exitoso, navegamos
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registro Estudiante - Paso 2", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = { viewModel.onFullNameChange(it) },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.studentIdNumber,
            onValueChange = { viewModel.onStudentIdNumberChange(it) },
            label = { Text("Numero de Estudiante") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.profilePictureUrl,
            onValueChange = { viewModel.onProfilePictureUrlChange(it) },
            label = { Text("Foto de perfil") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.error != null) {
            Text(uiState.error!!, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.registrarEstudiante() },
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
