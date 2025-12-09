package ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI para el registro
data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val userType: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val userIdCreated: String? = null, // Si esto tiene valor, pasamos a crear comerciante
    val isUserStepCompleted: Boolean = false
)


class SignUpViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onEmailChange(newValue: String) {
        _uiState.update { it.copy(email = newValue) }
    }

    fun onPasswordChange(newValue: String) {
        _uiState.update { it.copy(password = newValue) }
    }

    // Paso 1: Crear el usuario en la tabla 'users'
    fun crearUsuarioBase(email: String, pass: String, userType: String) {
        val state = _uiState.value
        android.util.Log.d("DEBUG_REGISTRO", "Intentando enviar -> Email: $email | Pass: $pass | Tipo: $userType")

        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Email y contraseña son obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Llamamos al repo
                val response = authRepository.registrarUsuarioBase(email, pass, userType)

                // ÉXITO: Guardamos el ID del usuario y marcamos el paso 1 como completado
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userIdCreated = response.id, // Guardamos el UUID
                        isUserStepCompleted = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al crear usuario: ${e.message}") }
            }
        }
    }

    // Función auxiliar para resetear errores si el usuario intenta de nuevo
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetState() {
        _uiState.update { it.copy(
            isUserStepCompleted = false,
            userIdCreated = null,
            error = null,
            isLoading = false,
            // Opcional: Si quieres limpiar los campos de texto también
            email = "",
            password = ""
        )}
    }

}

// Factory (sin cambios)
class SignUpViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignUpViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
