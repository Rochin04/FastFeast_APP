package ui.cart

import androidx.lifecycle.ViewModel
import com.example.fastfeastv01.data.Platillo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ui.auth.AuthViewModel

data class CartUiState(
    val platillos: List<Platillo> = emptyList(),
    val total: Double = 0.0
)

class CartViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    // Función para añadir un platillo al carrito
    fun agregarAlCarrito(platillo: Platillo) {
        _uiState.update { currentState ->
            val nuevosPlatillos = currentState.platillos + platillo
            val nuevoTotal = nuevosPlatillos.sumOf { it.precio }
            currentState.copy(
                platillos = nuevosPlatillos,
                total = nuevoTotal
            )
        }
    }

    // Aquí podrías añadir más funciones (ej. eliminarDelCarrito, vaciarCarrito, etc.)
}