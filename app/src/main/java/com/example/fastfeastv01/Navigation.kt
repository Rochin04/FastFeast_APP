package com.example.fastfeastv01

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Importa el modelo de datos para poder usarlo
// Asegúrate de que la ruta del import sea la correcta para tu proyecto
import com.example.fastfeastv01.data.Platillo

sealed class AppScreen(val route: String) {
    object Principal : AppScreen("principal")
    object Detalles : AppScreen("detalles/{platilloId}/{platilloNombre}/{platilloDescripcion}/{platilloPrecio}/{platilloImagenUrl}") {

        // --- ¡LA SOLUCIÓN ESTÁ AQUÍ! ---
        fun createRoute(platillo: Platillo): String {
            // 1. Codificamos los argumentos de tipo String que podrían contener caracteres especiales.
            val encodedNombre = URLEncoder.encode(platillo.nombre, StandardCharsets.UTF_8.toString())
            val encodedDescripcion = URLEncoder.encode(platillo.descripcion, StandardCharsets.UTF_8.toString())
            val encodedImagenUrl = URLEncoder.encode(platillo.imagenUrl, StandardCharsets.UTF_8.toString())

            // 2. Construimos la ruta con los valores ya codificados y seguros.
            return "detalles/${platillo.id}/$encodedNombre/$encodedDescripcion/${platillo.precio}/$encodedImagenUrl"
        }
    }
    object Login : AppScreen("login")
    object Carrito : AppScreen("carrito")
    object Perfil : AppScreen("perfil")
    object Configuracion : AppScreen("configuracion")
    object Registro : AppScreen("registro")
    object RegistroMerchant : AppScreen("registro_merchant/{userId}") {
        fun createRoute(userId: String) = "registro_merchant/$userId"
    }
    object RegistroStudent : AppScreen("registro_student/{userId}") {
        fun createRoute(userId: String) = "registro_student/$userId"
    }
}