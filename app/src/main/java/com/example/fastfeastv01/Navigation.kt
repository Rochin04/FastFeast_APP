package com.example.fastfeastv01

import java.net.URLEncoder

sealed class AppScreen(val route: String) {
    object Principal : AppScreen("principal")
    object Detalles : AppScreen("detalles/{platilloId}/{platilloNombre}/{platilloDescripcion}/{platilloPrecio}/{platilloImagenUrl}") {
        fun createRoute(platillo: Platillo): String {
            val encodedUrl = URLEncoder.encode(platillo.imagenUrl, "UTF-8")
            // Reemplazamos los saltos de línea en la descripción para que no rompan la URL
            val encodedDesc = URLEncoder.encode(platillo.descripcion, "UTF-8")
            return "detalles/${platillo.id}/${platillo.nombre}/$encodedDesc/${platillo.precio}/$encodedUrl"
        }
    }
    object Carrito : AppScreen("carrito")
    object Login : AppScreen("login")
}
