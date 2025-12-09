package data

import android.util.Log
import com.example.fastfeastv01.data.ComercianteRegisterRequest
import com.example.fastfeastv01.data.ComercianteRegisterResponse
import com.example.fastfeastv01.data.LoginRequest
import com.example.fastfeastv01.data.StudentRegisterRequest
import com.example.fastfeastv01.data.StudentRegisterResponse
import com.example.fastfeastv01.data.UserDetailResponse
import com.example.fastfeastv01.data.UserRegisterRequest
import com.example.fastfeastv01.data.UserRegisterResponse
import com.example.fastfeastv01.network.Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException
import retrofit2.HttpException

class AuthRepository(private val userPreferencesRepository: UserPreferencesRepository) {
    val isLoggedIn: Flow<Boolean> = userPreferencesRepository.isLoggedIn
    val userId: Flow<String?> = userPreferencesRepository.userId
    val userType: Flow<String?> = userPreferencesRepository.userType
    suspend fun registrarUsuarioBase(email: String, pass: String, userType: String): UserRegisterResponse {

        val request = UserRegisterRequest(
            email = email,
            passwordHash = pass,
            userType = userType
        )

        // 1. Llamada a la API (Ahora devuelve UserResponseWrapper)
        val responseWrapper = Api.retrofitService.registerUser(request)

        // 2. Obtenemos el usuario DIRECTAMENTE (Ya no es una lista)
        val usuarioCreado = responseWrapper.data

        val idFinal = usuarioCreado.id ?: usuarioCreado.uuid

        // 3. Lógica del token (si existiera)   !usuarioCreado.token.isNullOrEmpty()
        if (!idFinal.isNullOrEmpty()) {
            // CORRECCIÓN 1: Usamos la nueva firma que pide (Boolean, String, String)
            userPreferencesRepository.saveLoginState(
                isLoggedIn = true,
                type = userType,
                id = idFinal
            )
        }


        return usuarioCreado.copy(id = idFinal)
    }
    suspend fun registrarComerciante(
        ownerId: String,
        name: String,
        description: String,
        logoUrl: String,
        locationLatitude: Float,
        locationLongitude: Float,
        address: String,
        openingTime: String,
        closingTime: String
    ): ComercianteRegisterResponse { // <--- CAMBIO DE TIPO DE RETORNO

        val request = ComercianteRegisterRequest(
            ownerId = ownerId,
            name = name,
            description = description,
            logoUrl = logoUrl,
            locationLatitude = locationLatitude,
            locationLongitude = locationLongitude,
            address = address,
            openingTime = openingTime,
            closingTime = closingTime
        )

        // 1. Llamada a la API
        val responseWrapper = Api.retrofitService.registrarComerciante(request)
//        userPreferencesRepository.saveLoginState(true)
        val comercioCreado = responseWrapper.data
        val idFinal = comercioCreado.id ?: comercioCreado.uuid
        if (!idFinal.isNullOrEmpty()) {
            userPreferencesRepository.saveLoginState(
                isLoggedIn = true,
                type = "merchant",
                id = idFinal
            )
        }

        // 2. Obtenemos la data (que es de tipo ComercianteRegisterResponse)

        // 3. Normalización de ID (opcional, solo si lo necesitas)

        // Retornamos el objeto correcto
        return comercioCreado.copy(id = idFinal)
    }
    suspend fun registrarEstudiante(
        userId: String,
        fullName: String,
        studentIdNumber: String,
        profilePictureUrl: String
    ): StudentRegisterResponse {

        val request = StudentRegisterRequest(
            userId = userId,
            fullName = fullName,
            studentIdNumber = studentIdNumber,
            profilePictureUrl = profilePictureUrl
        )

        val responseWrapper = Api.retrofitService.registrarEstudiante(request)
        val estudianteCreado = responseWrapper.data
        val idFinal = estudianteCreado.id ?: estudianteCreado.userId
        // Éxito: Guardamos sesión
        if (!idFinal.isNullOrEmpty()) {
            userPreferencesRepository.saveLoginState(
                isLoggedIn = true,
                type = "student",
                id = idFinal
            )
        }
        return estudianteCreado.copy(id = idFinal)
    }
    suspend fun iniciarSesionManual(email: String, pass: String): Result<String> {
        return try {
            val request = LoginRequest(email = email, password = pass)

            // 1. API Call
            val response = Api.retrofitService.login(request)
            Log.d("LOGIN_DEBUG", "Respuesta Server - ID: ${response.userId}, Type: ${response.userType}")

            val receivedId = response.userId

            if (!receivedId.isNullOrEmpty()) {

                // 2. Determinar tipo (Logica de Fallback)
                val finalUserType: String? = if (!response.userType.isNullOrEmpty()) {
                    response.userType
                } else {
                    // Si el login no devolvió el tipo, buscamos por ID
                    try {
                        val wrapper = Api.retrofitService.getUserById(receivedId)
                        wrapper.data.userType
                    } catch (e: Exception) {
                        null
                    }
                }

                if (finalUserType != null) {
                    // CORRECCIÓN 4: Usamos saveLoginState para guardar todo de una vez
                    userPreferencesRepository.saveLoginState(
                        isLoggedIn = true,
                        type = finalUserType,
                        id = receivedId
                    )

                    Log.d("LOGIN_DEBUG", "Sesión guardada. ID: $receivedId, Tipo: $finalUserType")
                    Result.success(finalUserType)
                } else {
                    Result.failure(Exception("No se pudo obtener el tipo de usuario"))
                }

            } else {
                val errorMsg = response.message ?: "Credenciales inválidas"
                Result.failure(Exception(errorMsg))
            }

        } catch (e: HttpException) {
            val code = e.code()
            if (code == 401 || code == 400 || code == 422) {
                Result.failure(Exception("Correo o contraseña incorrectos"))
            } else {
                Result.failure(Exception("Error del servidor: $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPerfilUsuario(userId: String): Result<UserDetailResponse> {
        return try {
            val wrapper = Api.retrofitService.getUserById(userId)
            Result.success(wrapper.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerDatosComerciante(ownerId: String): Result<ComercianteRegisterResponse> {
        return try {
            // Reutilizamos el endpoint que agregamos antes en ApiService
            // GET /api/v1/comerciantes/{owner_id}
            val response = Api.retrofitService.getMerchantByOwnerId(ownerId)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cerrarSesion() {
        userPreferencesRepository.clearSession()
    }
}

