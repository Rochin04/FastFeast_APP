package com.example.fastfeastv01

import java.net.URLEncoder

sealed class AppScreen(val route: String) {
    object Principal : AppScreen("principal")
    object Detalles : AppScreen("detalles/{platilloId}/{platilloNombre}/{platilloDescripcion}/{platilloPrecio}/{platilloImagenUrl}") {
        fun createRoute(platillo: Platillo): String {
            return "detalles/${platillo.id}/${platillo.nombre}/${platillo.descripcion}/${platillo.precio}/${platillo.imagenUrl}"
        }
    }
    object Login : AppScreen("login")
    object Carrito : AppScreen("carrito")
    object Perfil : AppScreen("perfil") // <-- NUEVO
    object Configuracion : AppScreen("configuracion") // <-- NUEVO
}

