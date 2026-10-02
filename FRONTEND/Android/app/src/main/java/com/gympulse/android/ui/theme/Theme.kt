package com.gympulse.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val EsquemaOscuro = darkColorScheme(
    primary = Morado,
    onPrimary = MoradoProfundo,
    primaryContainer = MoradoContenedor,
    onPrimaryContainer = Morado,
    secondary = MoradoGris,
    tertiary = Rosa,
    background = Negro,
    onBackground = Blanco,
    surface = Negro,
    onSurface = Blanco,
    surfaceVariant = Superficie2,
    onSurfaceVariant = TextoSecundario,
    surfaceContainerLowest = Negro,
    surfaceContainerLow = Superficie1,
    surfaceContainer = Superficie1,
    surfaceContainerHigh = Superficie2,
    surfaceContainerHighest = Superficie3,
    surfaceBright = Superficie3,
    outline = TextoTenue,
    outlineVariant = Superficie3,
    error = Rojo,
    onError = Blanco,
    errorContainer = RojoContenedor,
    onErrorContainer = Rojo
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun GymPulseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaOscuro,
        typography = Typography,
        shapes = Formas,
        content = content
    )
}
