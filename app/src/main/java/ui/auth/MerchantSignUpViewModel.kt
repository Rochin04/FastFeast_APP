package ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MerchantSignUpUiState(
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val logoUrl : String = "",
    val locationLatitude:  String = "",
    val locationLongitude:  String = "",
    val openingTime: String = "09:00:00",
    val closingTime: String = "22:00:00",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
class MerchantSignUpViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MerchantSignUpUiState())
    val uiState: StateFlow<MerchantSignUpUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onLogoUrlChange(v: String) = _uiState.update { it.copy(logoUrl = v) }
    fun onLocationLatitudeChange(v: String) = _uiState.update { it.copy(locationLatitude = v) }
    fun onLocationLongitudeChange(v: String) = _uiState.update { it.copy(locationLongitude = v) }
    fun onAddressChange(v: String) = _uiState.update { it.copy(address = v) }
    fun onOpeningTimeChange(v: String) = _uiState.update { it.copy(openingTime = v) }
    fun onClosingTimeChange(v: String) = _uiState.update { it.copy(closingTime = v) }
    fun setOwnerId(id: String) = _uiState.update { it.copy(ownerId = id) }

    fun registrarComerciante(ownerId: String? = null) {
        val state = _uiState.value

        // Validación básica
        if (state.name.isBlank() || state.address.isBlank()) {
            _uiState.update { it.copy(error = "El nombre y la dirección son obligatorios") }
            return
        }

        val finalOwnerId = ownerId ?: state.ownerId
        if (finalOwnerId.isBlank()) {
            _uiState.update { it.copy(error = "Error: ID de usuario no encontrado") }
            return
        }

        // Validación de coordenadas
        val latString = state.locationLatitude.replace(",", ".").trim()
        val longString = state.locationLongitude.replace(",", ".").trim()
        val latFinal = latString.toFloatOrNull()
        val longFinal = longString.toFloatOrNull()

        if (latFinal == null || longFinal == null) {
            _uiState.update { it.copy(error = "La latitud o longitud no son números válidos (ej: 12.34)") }
            return
        }

        // Limpieza de hora (HH:MM:SS)
        val openTimeIso = formatToIsoDate(state.openingTime)
        val closeTimeIso = formatToIsoDate(state.closingTime)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.registrarComerciante(
                    ownerId = finalOwnerId,
                    name = state.name,
                    description = state.description,
                    logoUrl = state.logoUrl,
                    locationLatitude = latFinal,
                    locationLongitude = longFinal,
                    address = state.address,
                    openingTime = openTimeIso,
                    closingTime = closeTimeIso
                )
                // Si no hay excepción, fue exitoso
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }

            } catch (e: HttpException) {
                // Manejo de errores HTTP (400, 422, 500)
                val errorBody = e.response()?.errorBody()?.string() ?: "Error desconocido"
                Log.e("API_ERROR", "Error HTTP ${e.code()}: $errorBody")

                val uiErrorMsg = if (e.code() == 422) {
                    "Error de validación (422). Revisa el formato de la hora o datos faltantes."
                } else {
                    "Error del servidor (${e.code()})"
                }

                _uiState.update {
                    it.copy(isLoading = false, error = uiErrorMsg)
                }

            } catch (e: Exception) {
                // Otros errores (conexión, parseo, etc.)
                Log.e("API_ERROR", "Excepción: ${e.message}")
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al registrar: ${e.message}")
                }
            }
        }
    }

    // Función auxiliar movida dentro de la clase para mantener el orden
    private fun formatToIsoDate(timeInput: String): String {
        // La API espera SOLO HORA: "HH:MM:SS"

        // 1. Si el input ya tiene formato fecha-hora ("2025-...T..."), extraemos solo la hora
        if (timeInput.contains("T")) {
            return timeInput.substringAfter("T").substringBefore("Z")
        }

        // 2. Limpieza básica para asegurar HH:MM:SS
        var cleanTime = timeInput.trim()
        // Si el usuario pone "09:00", le agregamos ":00" para que sean segundos
        if (cleanTime.count { it == ':' } == 1) {
            cleanTime += ":00"
        }

        return cleanTime
    }
}

class MerchantSignUpViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MerchantSignUpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MerchantSignUpViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}