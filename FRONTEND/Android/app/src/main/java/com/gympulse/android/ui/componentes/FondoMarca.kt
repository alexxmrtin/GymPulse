package com.gympulse.android.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gympulse.android.R

@Composable
fun FondoMarca(
    modifier: Modifier = Modifier,
    contenido: @Composable BoxScope.() -> Unit
) {
    val brillo = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(
                Brush.radialGradient(
                    colors = listOf(brillo, Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = 1400f
                )
            )
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            contenido()
        }
    }
}

@Composable
fun LogoMarca(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(72.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_mancuerna),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun AvisoInformativo(mensaje: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
fun AvisoError(mensaje: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(14.dp)
        )
    }
}
