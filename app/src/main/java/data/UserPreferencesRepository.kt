package data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    // Creamos una clave para nuestro valor booleano
    private object PreferencesKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    // Un Flow que emite el estado de login cada vez que cambia.
    // La UI observará este Flow.
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        }

    // Función para guardar el estado de login. Es 'suspend' porque DataStore es asíncrono.
    suspend fun saveLoginState(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
        }
    }
}
