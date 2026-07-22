package com.gympulse.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de la app. background es el fondo de las pantallas y surface el de los elementos que van encima (tarjetas, barras).
// Los "on..." son el color del contenido que va SOBRE cada uno: onBackground es el color del texto sobre el fondo.
private val EsquemaOscuro = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color.Black,
    surface = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

// KOTLIN: content es de tipo @Composable () -> Unit, es decir, una funcion que no recibe nada y no devuelve nada. Al ser el ultimo parametro, quien llame a GymPulseTheme puede pasarla entre llaves.
@Composable
fun GymPulseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaOscuro,
        typography = Typography,
        content = content
    )
}
