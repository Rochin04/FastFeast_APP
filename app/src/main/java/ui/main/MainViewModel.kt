package ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastfeastv01.Categoria
import com.example.fastfeastv01.Platillo
import data.PlatilloRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Clase de estado para representar todo lo que la UI necesita mostrar
data class MainUiState(
    val categorias: List<Categoria> = emptyList(),
    val platillos: List<Platillo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class MainViewModel : ViewModel() {

    private val repository = PlatilloRepository()

    // StateFlow privado y mutable para que solo el ViewModel pueda modificarlo
    private val _uiState = MutableStateFlow(MainUiState())
    // StateFlow público e inmutable para que la UI lo observe de forma segura
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // El bloque init se ejecuta cuando se crea el ViewModel
    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        // Usamos el scope del ViewModel para lanzar una corrutina
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Obtenemos los datos del repositorio
            try {
                val categorias = repository.getCategorias()
                val platillos = repository.getPlatillos()

                // 4. Si todo va bien, actualizamos el estado como antes
                _uiState.update { currentState ->
                    currentState.copy(
                        categorias = categorias,
                        platillos = platillos,
                        isLoading = false,
                        error = null // Limpiamos cualquier error previo
                    )
                }
            } catch (e: Exception) {
                // 5. ¡AQUÍ ESTÁ LA CLAVE! Si algo falla en el repositorio, lo capturamos
                Log.e("MainViewModel", "Error al cargar datos", e) // Imprimimos el error detallado en Logcat
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "No se pudieron cargar los datos. Inténtalo de nuevo." // Mostramos un mensaje amigable
                    )
                }
            }
        }
    }
    // Aquí podrías añadir funciones para manejar eventos de la UI,
    // por ejemplo: onCategoriaClicked(categoria: Categoria) { ... }
}
