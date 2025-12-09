package data

import com.example.fastfeastv01.data.Categoria
import com.example.fastfeastv01.R
import com.example.fastfeastv01.data.ComidaDto
import com.example.fastfeastv01.data.CategoriaDto
import com.example.fastfeastv01.data.CreatePlatilloRequest
import com.example.fastfeastv01.data.Platillo
import com.example.fastfeastv01.network.Api

// Interfaz del repositorio
interface PlatillosRepository {
    suspend fun getPlatillos(): List<Platillo>
    suspend fun getCategorias(): List<Categoria> // <--- Nuevo método
    suspend fun createPlatillo(request: CreatePlatilloRequest): Boolean
//    suspend fun getMerchantIdByUserId(userId: String): String? // Nuevo método
    suspend fun getMerchantIdByUserId(userId: String): String?
}

// Implementación del repositorio
class NetworkPlatillosRepository : PlatillosRepository {

    override suspend fun getPlatillos(): List<Platillo> {
        // 1. Obtenemos el objeto ComidasResponse
        val response = Api.retrofitService.getComidas()
        // 2. Extraemos la lista de la propiedad "data"
        val comidasDtoList = response.data
        // 3. Mapeamos la lista de DTO a la lista de objetos de la UI
        return comidasDtoList.map { it.toPlatillo() }
    }

    override suspend fun getCategorias(): List<Categoria> {
        // 1. Obtenemos el objeto CategoriasResponse
        val response = Api.retrofitService.getCategorias()
        // 2. Extraemos la lista de la propiedad "data"
        val categoriasDtoList = response.data
        // 3. Mapeamos la lista de DTO a la lista de objetos de la UI
        return categoriasDtoList.map { dto ->
            Categoria(
                id = dto.id,
                nombre = dto.nombre,
                // Asignamos un icono local basado en el nombre que viene de la API
                icono = obtenerIconoPorNombre(dto.nombre)
            )
        }
    }
    override suspend fun createPlatillo(request: CreatePlatilloRequest): Boolean {
        return try {
            // Llamamos a la API
            val response = Api.retrofitService.createPlatillo(request)

            // Si obtenemos un ID en la respuesta, asumimos éxito
            !response.data.id.isNullOrEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla la petición (400, 500, sin internet), devolvemos false
            throw e // O return false, dependiendo de cómo quieras manejarlo en el ViewModel
        }
    }
    override suspend fun getMerchantIdByUserId(userId: String): String? {
        return try {
            // Llamamos al endpoint nuevo
            val response = Api.retrofitService.getMerchantByOwnerId(userId)
            // Retornamos el ID REAL del comercio (Ej: a09951f1...)
            response.data.id
        } catch (e: Exception) {
            e.printStackTrace()
            null // Si falla o no existe, retornamos null
        }
    }

    // Función auxiliar para asignar iconos locales según el texto que viene de la API
    private fun obtenerIconoPorNombre(nombre: String): Int {
        return when (nombre.lowercase()) {
            "hamburguesas", "burger" -> R.drawable.ic_launcher_foreground // TODO: Cambia por tus iconos reales
            "pizzas", "pizza" -> R.drawable.ic_launcher_foreground       // TODO: Cambia por tus iconos reales
            "bebidas", "drinks" -> R.drawable.ic_launcher_foreground     // TODO: Cambia por tus iconos reales
            else -> R.drawable.ic_launcher_foreground // Icono por defecto
        }
    }
}

// Función de extensión para convertir los datos
fun ComidaDto.toPlatillo(): Platillo {
    return Platillo(
        // El ID en el JSON es un String (UUID), pero tu modelo de UI espera un Int.
        // Como no podemos convertir un UUID a Int, usamos un valor temporal como 0 o el hashcode.
        // O mejor aún, cambia el 'id' en tu data class 'Platillo' a String.
        id = this.id, // Solución temporal. Recomiendo cambiar Platillo.id a String.
        nombre = this.nombre,
        descripcion = this.description,
        precio = this.price.toDoubleOrNull() ?: 0.0,
        imagenUrl = this.imagenUrl
    )
}