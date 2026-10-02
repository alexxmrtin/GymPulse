package com.gympulse.android.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gympulse.android.ui.comun.GrupoSeries
import com.gympulse.android.ui.comun.LocalUnidadPeso
import com.gympulse.android.ui.comun.formatearPeso

@Composable
fun TarjetaSeries(
    grupo: GrupoSeries,
    modifier: Modifier = Modifier,
    onEliminarSerie: ((Long) -> Unit)? = null,
    onAnadirSerie: (() -> Unit)? = null
) {
    val unidad = LocalUnidadPeso.current

    Tarjeta(modifier = modifier) {
        Text(text = grupo.ejercicioNombre, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Máximo ${formatearPeso(grupo.pesoMaximo, unidad)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        grupo.series.forEachIndexed { indice, serie ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${indice + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "${serie.repeticiones} reps",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatearPeso(serie.peso, unidad),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                if (onEliminarSerie != null) {
                    IconButton(onClick = { onEliminarSerie(serie.id) }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Eliminar serie ${indice + 1}",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (onAnadirSerie != null) {
            TextButton(onClick = onAnadirSerie, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Añadir serie")
            }
        }
    }
}
