package ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fastfeastv01.data.StudentRegisterResponse
import data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class StudentSignUpUiState(
    val userId: String = "",
    val fullName: String = "",
    val studentIdNumber: String = "",
    val profilePictureUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class StudentSignUpViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentSignUpUiState())
    val uiState = _uiState.asStateFlow()

    fun onFullNameChange(v: String) = _uiState.update { it.copy(fullName = v) }
    fun onStudentIdNumberChange(v: String) = _uiState.update { it.copy(studentIdNumber = v) }
    fun onProfilePictureUrlChange(v: String) = _uiState.update { it.copy(profilePictureUrl = v) }
    fun setUserId(id: String) = _uiState.update { it.copy(userId = id) }

    fun registrarEstudiante(userId: String? = null) {
        val state = _uiState.value

        // Validaciones
        if (state.fullName.isBlank() || state.studentIdNumber.isBlank()) {
            _uiState.update { it.copy(error = "Llena todos los campos obligatorios") }
            return
        }

        val finalUserId = userId ?: state.userId
        if (finalUserId.isBlank()) {
            _uiState.update { it.copy(error = "Error: ID de usuario no encontrado") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.registrarEstudiante(
                    userId = finalUserId,
                    fullName = state.fullName,
                    studentIdNumber = state.studentIdNumber,
                    profilePictureUrl = state.profilePictureUrl
                )
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("API_ERROR", "Error ${e.code()}: $errorBody")
                _uiState.update { it.copy(isLoading = false, error = "Error de validación: Revisa tus datos") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

class StudentSignUpViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StudentSignUpViewModel(repo) as T
    }
}