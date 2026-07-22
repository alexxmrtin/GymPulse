package com.gympulse.android.ui.componentes

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gympulse.android.Pantalla //Importamos la clase Pantalla
import com.gympulse.android.R

// Cada boton de la barra: a que pantalla lleva, que texto muestra y con que icono. El icono es un Int porque los recursos de res/ se referencian por un id numerico que genera Android al compilar.
// KOTLIN: una data class es una clase que solo guarda datos; genera sola el constructor, equals, hashCode y toString.
private data class BotonBarra(
    val pantalla: Pantalla,
    val etiqueta: String,
    val icono: Int
)

private val BOTONES = listOf(
    BotonBarra(Pantalla.ENTRENAMIENTO, "Entrenamiento", R.drawable.ic_mancuerna),
    BotonBarra(Pantalla.PERFIL, "Perfil", R.drawable.ic_perfil),
    BotonBarra(Pantalla.AJUSTES, "Ajustes", R.drawable.ic_ajustes)
)

// Barra de navegacion inferior, compartida por todas las pantallas menos login y registro.
// No sabe navegar: recibe cual es la pantalla activa y avisa hacia arriba de cual se ha pulsado, igual que hacen PantallaLogin y PantallaRegistro.
// KOTLIN: onPantallaSeleccionada es "(Pantalla) -> Unit", una funcion que recibe una Pantalla y no devuelve nada.
@Composable
fun BarraInferior(
    pantallaActual: Pantalla,
    onPantallaSeleccionada: (Pantalla) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(32.dp))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(32.dp)),

        containerColor = Color.White.copy(alpha = 0.10f),

        // NavigationBar ya reserva por su cuenta el hueco de la barra de gestos. Como aqui lo hacemos nosotros con navigationBarsPadding(), se lo quitamos para que no se sume dos veces.
        windowInsets = WindowInsets(0)
    ) {

        //Cada boton es un objeto que creamos usando la clase BotonBarra de arriba del todo. Cada uno tienes tres propiedades la etiqueta,el icono, y la pantalla a renderizar
        BOTONES.forEach { boton ->
            NavigationBarItem(

                selected = pantallaActual == boton.pantalla,
                onClick = { onPantallaSeleccionada(boton.pantalla) },
                icon = {
                    Icon(painter = painterResource(boton.icono), contentDescription = boton.etiqueta)
                },
                label = { Text(boton.etiqueta) },

                // Sin esto, los colores por defecto de Material chocan con el fondo de cristal.
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    unselectedTextColor = Color.White.copy(alpha = 0.6f),
                    indicatorColor = Color.White.copy(alpha = 0.18f)
                )
            )
        }
    }
}
