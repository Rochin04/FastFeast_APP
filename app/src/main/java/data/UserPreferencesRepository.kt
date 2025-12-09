package data

import android.content.Context
import android.util.Log // <--- Importante para ver los logs
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import data.UserPreferencesRepository.PreferencesKeys.USER_ID
import data.UserPreferencesRepository.PreferencesKeys.USER_TYPE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// La instancia única de DataStore ligada al contexto
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    // Objeto privado para mantener las claves organizadas y seguras
    private object PreferencesKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id") // Clave consistente
        val USER_TYPE = stringPreferencesKey("user_type") // Nueva clave
    }

    // 1. LEER ESTADO LOGIN
    val userId: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val id = preferences[PreferencesKeys.USER_ID]
            // Si quieres ver el log cada vez que cambie o se lea:
            Log.d("UserPrefs", "Leyendo ID desde flujo: $id")
            id
        }

    val userType: Flow<String?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[USER_TYPE] }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        }

    // 2. GUARDAR ESTADO LOGIN
    suspend fun saveLoginState(isLoggedIn: Boolean, type: String, id: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
            preferences[PreferencesKeys.USER_TYPE] = type
            preferences[PreferencesKeys.USER_ID] = id
        }
    }

    // 3. GUARDAR USER ID (Con Log de depuración)
    suspend fun saveUserId(id: String) {
        Log.d("UserPrefs", "Guardando ID en disco: $id") // <--- DEBUG
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = id

        }
    }

    // 4. OBTENER USER ID (Con Log de depuración)
//    fun getUserId(): Flow<String?> {
//        return context.dataStore.data.map { preferences ->
//            val id = preferences[PreferencesKeys.USER_ID]
//            Log.d("UserPrefs", "Leyendo ID desde disco: $id") // <--- DEBUG
//            id
//        }
//    }
//    fun getUserType(): Flow<String?> {
//        return context.dataStore.data.map { preferences ->
//            val type = preferences[PreferencesKeys.USER_TYPE]
//            Log.d("UserPrefs", "Leyendo Tipo de Usuario: $type")
//            type
//        }
//    }
    suspend fun saveUserType(type: String) {
        context.dataStore.edit { it[PreferencesKeys.USER_TYPE] = type }
    }
    suspend fun clearSession() {
        Log.d("UserPrefs", "Limpiando sesión...")
        context.dataStore.edit { preferences ->
            preferences.clear()
            // Opcional: si quieres mantener flags de "Bienvenida vista", no uses clear(),
            // sino remove() para las claves específicas.
        }
    }
}
