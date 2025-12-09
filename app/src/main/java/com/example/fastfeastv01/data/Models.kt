package com.example.fastfeastv01.data
import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

// Wrapper generico para las respuestas de la API que usan la forma { "data": [...] }
data class ApiResponse<T>(
    val data: List<T>
)

data class ComidaDto(
    val id: String,
    @Json(name = "name") val nombre: String, // Usamos @Json para mapear 'name' a 'nombre'
    val description: String?,
    val price: String,
    val category: String?,
    @Json(name = "image_url") val imagenUrl: String?,
    @Json(name = "is_available") val isAvailable: Boolean? = true
)
data class ComidasResponse(
    val data: List<ComidaDto> // La clave "data" coincide con el JSON
)

data class CategoriaDto(
    val id: String,
    @Json(name = "name") val nombre: String
)
data class CategoriasResponse(
    val data: List<CategoriaDto> // La clave "data" coincide con el JSON
)

// --- MODELO PARA TU UI (El objeto limpio que usa tu app) ---
// Esto suele ir en una carpeta 'model', pero lo pondré aquí para simplificar
@Parcelize
data class Platillo(
    val id: String,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val imagenUrl: String?
): Parcelable
data class Categoria(
    val id: String,
    val nombre: String,
    @DrawableRes val icono: Int
)
data class UserRegisterRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password_hash") val passwordHash: String,
    @Json(name = "user_type") val userType: String
)
data class UserRegisterResponse(
    val id: String? = null,              // Caso estándar
    @Json(name = "uuid") val uuid: String? = null, // Caso Supabase a veces
    val email: String? = null,
    val token: String? = null
)
data class UserResponseWrapper(
    val data: UserRegisterResponse
)
data class ComercianteRegisterRequest(
    @Json(name = "owner_id")
    val ownerId: String,

    @Json(name = "name")
    val name: String, // Si es igual en ambos lados, no es obligatorio, pero es buena práctica ponerlo

    @Json(name = "description")
    val description: String,

    @Json(name = "logo_url")
    val logoUrl: String,

    @Json(name = "location_latitude")
    val locationLatitude: Float,

    @Json(name = "location_longitude")
    val locationLongitude: Float,

    @Json(name = "address")
    val address: String,

    @Json(name = "opening_time")
    val openingTime: String,

    @Json(name = "closing_time")
    val closingTime: String
)

data class ComercianteRegisterResponse(
    val id : String?,
    @Json(name = "uuid") val uuid: String? = null,
    @Json(name = "owner_id") val ownerId: String?,
    val name : String?,
    @Json(name = "location_latitude") val locationLatitude: Float? = 0.0F,
    @Json(name = "location_longitude") val locationLongitude: Float? = 0.0F,
    @Json(name = "is_validated") val isValidated: Boolean? = false,
    val description : String?,
    @Json(name = "logo_url") val logoUrl: String? = "logo_url",
    val address : String?,
    @Json(name = "opening_time") val openingTime: String? = "merchant",
    @Json(name = "closing_time") val closingTime: String? = "merchant"
)
data class ComercianteResponseWrapper(
    val data: ComercianteRegisterResponse
)
data class StudentRegisterRequest(
    @Json(name = "user_id") val userId: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "student_id_number") val studentIdNumber: String,
    @Json(name = "profile_picture_url") val profilePictureUrl: String // Ejemplo de campo extra
)
data class StudentRegisterResponse(
    val id: String?,
    @Json(name = "user_id") val userId: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "student_id_number") val studentIdNumber: String,
    @Json(name = "profile_picture_url") val profilePictureUrl: String,
    @Json(name = "is_verified") val isVerified: Boolean? = false,
)
data class StudentResponseWrapper(
    val data: StudentRegisterResponse
)
data class UserDto(
    val id: String,
    val email: String,

    // AGREGA EL '?' AQUÍ
    @Json(name = "password_hash")
    val passwordHash: String?, // <-- Ahora acepta nulos sin dar error

    // Es buena práctica hacer este nullable también por seguridad
    @Json(name = "user_type")
    val userType: String?      // <-- Ahora acepta nulos
)

data class UserListResponse(
    val data: List<UserDto>
)
data class LoginRequest(
    val email: String,
    @Json(name = "password") val password: String
)

data class LoginResponse(
    val success: Boolean? = false,
    val message: String? = null,

    @Json(name = "user_id") val userId: String?,
    @Json(name = "user_type") val userType: String?
)

// --- 2. DETALLES DE USUARIO (Para getUserById) ---

data class UserDetailResponse(
    // 1. Aceptamos 'id', 'user_id' o 'uuid'. Al menos uno vendrá.
    val id: String? = null,

    @Json(name = "user_id")
    val userId: String? = null,

    @Json(name = "uuid")
    val uuid: String? = null,

    val email: String?,

    @Json(name = "user_type")
    val userType: String?,

    @Json(name = "full_name")
    val fullName: String?,
    val description : String?,

    @Json(name = "student_id_number")
    val studentIdNumber: String?,
    val name : String?,
    @Json(name = "logo_url")
    val logoUrl: String? = "logo_url",
) {
    // Helper para obtener el ID real sin importar cómo venga
    fun getRealId(): String = id ?: userId ?: uuid ?: "unknown_id"
}
data class UserDetailWrapper(
    val data: UserDetailResponse
)
data class CreatePlatilloRequest(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "price") val price: Double, // En el request se envía como número
    @Json(name = "category") val category: String, // Nombre de la categoría (ej. "Bebidas")
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "merchant_id") val merchantId: String,
    @Json(name = "is_available") val isAvailable: Boolean = true,
    @Json(name = "category_id") val categoryId: String // UUID de la categoría
)
data class CreatePlatilloResponse(
    val id: String,
    @Json(name = "merchant_id") val merchantId: String,
    @Json(name = "category_id") val categoryId: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String?,
    @Json(name = "price") val price: String, // La API lo devuelve como String largo
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "category") val category: String?,
    @Json(name = "is_available") val isAvailable: Boolean?
)
data class CreatePlatilloWrapper(
    val data: CreatePlatilloResponse
)
data class CategoryResponse(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String
)
data class CategoryWrapper(
    val data: CategoryResponse
)