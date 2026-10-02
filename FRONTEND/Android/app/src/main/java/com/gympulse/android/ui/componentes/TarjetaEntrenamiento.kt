package com.gympulse.android.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gympulse.android.data.modelo.Entrenamiento
import com.gympulse.android.ui.comun.LOCALE_ES
import com.gympulse.android.ui.comun.LocalUnidadPeso
import com.gympulse.android.ui.comun.fechaRelativa
import com.gympulse.android.ui.comun.formatearVolumen
import com.gympulse.android.ui.comun.numeroEjercicios
import com.gympulse.android.ui.comun.titulo
import com.gympulse.android.ui.comun.volumen
import java.time.format.TextStyle

@Composable
fun TarjetaEntrenamiento(
    entrenamiento: Entrenamiento,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unidad = LocalUnidadPeso.current

    Tarjeta(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CasillaFecha(entrenamiento)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = entrenamiento.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entrenamiento.fecha.fechaRelativa(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Metrica("${entrenamiento.numeroEjercicios} ejer.")
                    Metrica("${entrenamiento.series.size} series")
                    Metrica(formatearVolumen(entrenamiento.volumen, unidad))
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun CasillaFecha(entrenamiento: Entrenamiento) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = entrenamiento.fecha.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = entrenamiento.fecha.month.getDisplayName(TextStyle.SHORT, LOCALE_ES).trimEnd('.').uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun Metrica(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.outline
    )
}
