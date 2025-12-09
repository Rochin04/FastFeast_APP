package ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import data.AuthRepository
import data.PlatillosRepository

class MainViewModelFactory(
    private val repository: PlatillosRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // 2. Pásalo al constructor del ViewModel
            return MainViewModel(repository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
