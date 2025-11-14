package ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    // Expone el estado de login como un StateFlow para que la UI lo observe
    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false // Valor inicial mientras se lee de DataStore
        )

    fun login() {
        viewModelScope.launch {
            repository.saveLoginState(true)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.saveLoginState(false)
        }
    }
}

// --- Factory para el ViewModel ---
// Como AuthViewModel ahora necesita el 'repository' en su constructor,
// necesitamos una Factory para decirle a Android cómo crearlo.
class AuthViewModelFactory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}