package ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import data.AuthRepository

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    // 1. ESTADO DE LOS CAMPOS DE TEXTO (Lo que escribe el usuario)
    var emailInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")

    // Estado para controlar errores o carga en la UI
    var isLoading by mutableStateOf(false)
    var loginError by mutableStateOf<String?>(null)
    // Expone el estado de login como un StateFlow para que la UI lo observe
//    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = false // Valor inicial mientras se lee de DataStore
//        )
    val isLoggedIn = repository.isLoggedIn
    fun login() {
        if (emailInput.isBlank() || passwordInput.isBlank()) {
            loginError = "Por favor llena todos los campos"
            return
        }

        viewModelScope.launch {
            isLoading = true
            loginError = null

            // LLAMADA AL REPOSITORIO
            // El repositorio hará el POST a la API y si es correcto,
            // él mismo ejecutará saveLoginState(true, type, id) internamente.
            val result = repository.iniciarSesionManual(emailInput, passwordInput)

            result.onSuccess { userType ->
                // Login exitoso. No necesitas hacer nada más aquí,
                // el 'isLoggedIn' cambiará automáticamente a true y la UI reaccionará.
                isLoading = false
            }

            result.onFailure { exception ->
                isLoading = false
                loginError = exception.message ?: "Error al iniciar sesión"
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            // Aquí usamos clearSession, que borra todo (ID, Type, Token)
            repository.cerrarSesion()
        }
    }
}

// --- Factory para el ViewModel ---
// Como AuthViewModel ahora necesita el 'repository' en su constructor,
// necesitamos una Factory para decirle a Android cómo crearlo.
class AuthViewModelFactory(private val userPreferencesRepository: UserPreferencesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val repository = AuthRepository(userPreferencesRepository)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}