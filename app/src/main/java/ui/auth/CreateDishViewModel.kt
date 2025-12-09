package ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fastfeastv01.data.Categoria
import com.example.fastfeastv01.data.CreatePlatilloRequest
import data.NetworkPlatillosRepository
import com.example.fastfeastv01.data.Platillo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.UUID

class CreateDishViewModel(private val repository: NetworkPlatillosRepository) : ViewModel() {

    // Estado para los campos del formulario
    var nombre = MutableStateFlow("")
    var descripcion = MutableStateFlow("")
    var precio = MutableStateFlow("")
    var imagenUrl = MutableStateFlow("")
    var categoriaNombre = MutableStateFlow("")
    var categoriaId = MutableStateFlow("")
    private var _currentMerchantId: String? = null

    // Estado de la UI (Carga, Éxito, Error)
    private val _uiState = MutableStateFlow<CreateDishUiState>(CreateDishUiState.Idle)
    val uiState: StateFlow<CreateDishUiState> = _uiState.asStateFlow()
    private val _listaCategorias = MutableStateFlow<List<Categoria>>(emptyList())
    val listaCategorias: StateFlow<List<Categoria>> = _listaCategorias.asStateFlow()

    init {
        cargarCategorias()
    }

    private fun cargarCategorias() {
        viewModelScope.launch {
            try {
                val lista = repository.getCategorias()
                _listaCategorias.value = lista
            } catch (e: Exception) {
                // Manejar error silenciosamente o mostrar retry
                println("Error cargando categorías: ${e.message}")
            }
        }
    }

    // Función para cuando el usuario selecciona una opción del Dropdown
    fun seleccionarCategoria(categoria: Categoria) {
        categoriaNombre.value = categoria.nombre
        categoriaId.value = categoria.id
    }

    fun crearPlatillo(merchantId: String) {
        val nombreVal = nombre.value
        val descVal = descripcion.value
        val precioVal = precio.value.toDoubleOrNull()
        val imgVal = imagenUrl.value
        val catNombreVal = categoriaNombre.value // Valor nuevo
        val catIdVal = categoriaId.value

        if (nombreVal.isBlank() || descVal.isBlank() || precioVal == null ||
            catNombreVal.isBlank() || catIdVal.isBlank()) {
            _uiState.value = CreateDishUiState.Error("Por favor llena todos los campos correctamente.")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateDishUiState.Loading
            try {
                val realMerchantId = repository.getMerchantIdByUserId(merchantId)
                if (realMerchantId.isNullOrEmpty()) {
                    _uiState.value = CreateDishUiState.Error("No se encontró un comercio asociado a este usuario.")
                    return@launch
                }
                // Crear objeto Platillo
                val request = CreatePlatilloRequest(
                    name = nombreVal,
                    description = descVal,
                    price = precioVal, // Double
                    imageUrl = imgVal,
                    merchantId = realMerchantId,
                    category = catNombreVal, // Ya no es hardcoded
                    categoryId = catIdVal,
                    isAvailable = true
                )

                // LLAMADA AL REPOSITORIO (Asegúrate que tu repo tenga esta función)
                repository.createPlatillo(request)

                _uiState.value = CreateDishUiState.Success
            }  catch (e: HttpException) {
                // ESTA PARTE ES CLAVE PARA EL ERROR 422
                val errorBody = e.response()?.errorBody()?.string()
                println("ERROR 422 DETALLE: $errorBody")

                _uiState.value = CreateDishUiState.Error("Error de validación: $errorBody")

            } catch (e: Exception) {
                // Otros errores (sin conexión, etc)
                _uiState.value = CreateDishUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateDishUiState.Idle
        nombre.value = ""
        descripcion.value = ""
        precio.value = ""
        imagenUrl.value = ""
        categoriaNombre.value = ""
        categoriaId.value = ""
    }
}

// Estados sellados para manejar la UI
sealed class CreateDishUiState {
    object Idle : CreateDishUiState()
    object Loading : CreateDishUiState()
    object Success : CreateDishUiState()
    data class Error(val message: String) : CreateDishUiState()
}

// Factory para inyectar el repositorio
class CreateDishViewModelFactory(private val repository: NetworkPlatillosRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateDishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateDishViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
