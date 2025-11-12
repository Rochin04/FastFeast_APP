package data

import com.example.fastfeastv01.Categoria
import com.example.fastfeastv01.Platillo
import com.example.fastfeastv01.R

class PlatilloRepository {

    // Esta función obtiene la lista de categorías
    fun getCategorias(): List<Categoria> {
        return listOf(
            Categoria("Hamburguesas", R.drawable.ic_launcher_foreground),
            Categoria("Pizza", R.drawable.ic_launcher_foreground),
            Categoria("Postres", R.drawable.ic_launcher_foreground),
            Categoria("Bebidas", R.drawable.ic_launcher_foreground),
            Categoria("Ensaladas", R.drawable.ic_launcher_foreground)
        )
    }

    // Esta función obtiene la lista de platillos
    fun getPlatillos(): List<Platillo> {
        return listOf(
            Platillo(1, "Hamburguesa Clásica", "Carne de res, lechuga, tomate y queso cheddar.", 10.99, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500"),
            Platillo(2, "Pizza Pepperoni", "Salsa de tomate, mozzarella y abundante pepperoni.", 12.50, "https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?w=500"),
            Platillo(3, "Tarta de Chocolate", "Intenso sabor a chocolate con una base crujiente.", 6.75, "https://images.unsplash.com/photo-1579609249769-d3439b1a7eea?w=500"),
            Platillo(4, "Hamburguesa BBQ", "Carne jugosa con aros de cebolla y salsa BBQ.", 11.99, "https://images.unsplash.com/photo-1603569283847-aa295f0d016a?w=500")
        )
    }
}
