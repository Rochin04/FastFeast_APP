package ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fastfeastv01.data.UserDetailResponse
import data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userData: UserDetailResponse? = null,
    val error: String? = null
)

class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // USAMOS collectLatest EN LUGAR DE first()
            // Esto se queda escuchando cambios en la base de datos local
            authRepository.userId.collectLatest { userId ->

                Log.d("PROFILE_DEBUG", "ID detectado en ViewModel: $userId")

                if (!userId.isNullOrEmpty()) {
                    // 1. PRIMERA LLAMADA: Datos básicos (Email, UserType)
                    val resultUser = authRepository.obtenerPerfilUsuario(userId)

                    resultUser.onSuccess { userBase ->

                        // Verificamos si es comerciante para buscar más datos
                        if (userBase.userType == "merchant") {

                            // 2. SEGUNDA LLAMADA: Datos del negocio (Nombre tienda, logo, desc)
                            val resultMerchant = authRepository.obtenerDatosComerciante(userId)

                            resultMerchant.onSuccess { merchantData ->
                                // 3. FUSIÓN: Creamos un objeto con los datos de ambas llamadas
                                val usuarioCompleto = userBase.copy(
                                    // Llenamos los campos que venían vacíos en el usuario base
                                    name = merchantData.name,            // Nombre del negocio
                                    description = merchantData.description, // Descripción del negocio
                                    logoUrl = merchantData.logoUrl       // Logo
                                )

                                _uiState.update {
                                    it.copy(isLoading = false, userData = usuarioCompleto, error = null)
                                }
                            }.onFailure { e ->
                                // Si falla la carga del comerciante, mostramos al menos los datos básicos
                                Log.w("PROFILE_DEBUG", "No se pudo cargar detalles del comercio: ${e.message}")
                                _uiState.update {
                                    it.copy(isLoading = false, userData = userBase, error = null) // Sin error fatal
                                }
                            }

                        } else {
                            // Si es estudiante o admin, mostramos lo que llegó en la primera llamada
                            _uiState.update {
                                it.copy(isLoading = false, userData = userBase, error = null)
                            }
                        }

                    }.onFailure { e ->
                        Log.e("PROFILE_DEBUG", "Error API User: ${e.message}")
                        _uiState.update {
                            it.copy(isLoading = false, error = "Error al cargar perfil: ${e.message}")
                        }
                    }
                }  else {
                    // Si es nulo, seguimos esperando (o mostramos loading)
                    // No mostramos error inmediatamente para dar tiempo a que se guarde
                    Log.w("PROFILE_DEBUG", "El ID es nulo todavía...")
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    class Factory(private val repo: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(repo) as T
        }
    }
}
