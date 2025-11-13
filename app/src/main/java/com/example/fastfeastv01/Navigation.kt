package com.example.fastfeastv01

sealed class AppScreen(val route: String) {
    object Principal : AppScreen("pantalla_principal")
    object Detalles : AppScreen("pantalla_detalles/{platilloId}") {
        fun createRoute(platilloId: Int) = "pantalla_detalles/$platilloId"
    }
}
