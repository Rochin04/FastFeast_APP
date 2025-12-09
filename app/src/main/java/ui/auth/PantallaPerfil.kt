package ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PermIdentity
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()

                uiState.error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { /* Opcional: reintentar */ }) { Text("Reintentar") }
                    }
                }

                uiState.userData != null -> {
                    val user = uiState.userData!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (user.userType == "merchant" && !user.logoUrl.isNullOrEmpty()) {
                            // Ejemplo usando Coil para cargar imagen remota
                            AsyncImage(
                                model = user.logoUrl,
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                            )
                        } else {
//                            // Avatar con inicial (tu código original)
//                            Box(
//                                modifier = Modifier
//                                    .size(100.dp)
//                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                // Preferimos fullName o name antes que el email para la inicial
//                                val nombreParaInicial = user.fullName ?: user.name ?: user.email ?: "?"
//                                Text(
//                                    text = nombreParaInicial.take(1).uppercase(),
//                                    style = MaterialTheme.typography.displayMedium,
//                                    color = MaterialTheme.colorScheme.onPrimaryContainer
//                                )
//                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = (user.email ?: "?").take(1).uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tarjetas de información

                        // CORRECCIÓN 2: Si email es nulo, mostramos "Sin correo"
                        ProfileInfoItem(
                            icon = Icons.Default.Email,
                            label = "Correo",
                            value = user.email ?: "Sin correo"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        when (user.userType) {
                            "student" -> {
                                // Mostramos full_name y student_id_number
                                ProfileInfoItem(
                                    icon = Icons.Default.Person,
                                    label = "Nombre Estudiante",
                                    value = user.fullName ?: "Sin nombre registrado"
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                ProfileInfoItem(
                                    icon = Icons.Default.Badge,
                                    label = "Matrícula",
                                    value = user.studentIdNumber ?: "Sin matrícula"
                                )
                            }

                            "merchant" -> {
                                // Mostramos name (negocio) y description
                                ProfileInfoItem(
                                    icon = Icons.Default.Store,
                                    label = "Nombre del Negocio",
                                    value = user.name ?: "Sin nombre de negocio"
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                ProfileInfoItem(
                                    icon = Icons.Default.Description,
                                    label = "Descripción",
                                    value = user.description ?: "Sin descripción disponible"
                                )
                            }

                            else -> {
                                // Caso por defecto (Admin o desconocido)
                                ProfileInfoItem(
                                    icon = Icons.Default.Person,
                                    label = "Rol",
                                    value = user.userType ?: "Desconocido"
                                )
                            }
                        }

//                        Spacer(modifier = Modifier.height(12.dp))

                        // CORRECCIÓN 4: Buscamos cualquier ID que no sea nulo
                        // (Si agregaste la función getRealId() al modelo, úsala aquí. Si no, usa esta línea):
//                        val idFinal = user.id ?: user.userId ?: user.uuid ?: "Sin ID"
//
//                        ProfileInfoItem(
//                            icon = Icons.Default.Badge,
//                            label = "ID Usuario",
//                            value = idFinal
//                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(text = value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
