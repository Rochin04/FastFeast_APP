package ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastfeastv01.data.Categoria
import data.NetworkPlatillosRepository
import com.example.fastfeastv01.data.Platillo
import data.AuthRepository
import data.PlatillosRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider


// Clase de estado para representar todo lo que la UI necesita mostrar
data class MainUiState(
    val isLoading: Boolean = false,
    val platillos: List<Platillo> = emptyList(), // Vienen de la API
    val categorias: List<Categoria> = emptyList(), // Vienen de la API o Hardcoded
    val error: String? = null

)
class MainViewModel(
    // Inyectamos el repositorio en el constructor
    private val platillosRepository: PlatillosRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole = _userRole.asStateFlow()

    init {
        cargarDatos()
        observarRolUsuario()
    }

    private fun observarRolUsuario() {
        viewModelScope.launch {
            authRepository.userType.collect { type ->
                _userRole.value = type
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.cerrarSesion()
            // El _userRole se actualizará solo gracias al collect de arriba
        }
    }

    // Unificamos la carga de datos
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Cargamos las categorías (Sabemos que esto funciona por el Logcat)
            val categoriasCargadas = try {
                platillosRepository.getCategorias()
            } catch (e: Exception) {
                emptyList() // Si falla, lista vacía
            }

            // 2. Intentamos cargar los platillos por separado
            var platillosCargados: List<Platillo> = emptyList()
            var errorMensaje: String? = null

            try {
                platillosCargados = platillosRepository.getPlatillos()
            } catch (e: Exception) {
                // Aquí atrapamos el error 500 para que NO detenga la app
                Log.e("MainViewModel", "Error al cargar platillos: ${e.message}")
                errorMensaje = "No se pudieron cargar los platillos (Error del servidor)"
            }

            // 3. Actualizamos la UI con lo que hayamos conseguido
            _uiState.update {
                it.copy(
                    isLoading = false,
                    categorias = categoriasCargadas,
                    platillos = platillosCargados,
                    // Solo mostramos error general si fallaron AMBAS cosas,
                    // o puedes guardar el error en una variable separada si prefieres.
                    error = if (categoriasCargadas.isEmpty() && platillosCargados.isEmpty())
                        "Error de conexión con el servidor"
                    else null
                )
            }
        }
    }
    // Mantén tu función getPlatillos si quieres reintentar solo eso,
    // pero es mejor usar 'cargarDatos' para refrescar todo.
    fun getPlatillos() = cargarDatos()

    class Factory(
        private val platillosRepo: PlatillosRepository,
        private val authRepo: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(platillosRepo, authRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
