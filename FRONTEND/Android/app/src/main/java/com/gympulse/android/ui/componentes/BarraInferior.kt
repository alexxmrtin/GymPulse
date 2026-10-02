package com.gympulse.android.ui.componentes

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gympulse.android.ui.navegacion.Pestana
import com.gympulse.android.ui.theme.CristalBorde
import com.gympulse.android.ui.theme.CristalFondo

@Composable
fun BarraInferior(
    pestanaActual: Pestana?,
    onPestanaSeleccionada: (Pestana) -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = MaterialTheme.shapes.extraLarge

    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(forma)
            .border(1.dp, CristalBorde, forma),
        containerColor = CristalFondo,
        windowInsets = WindowInsets(0)
    ) {
        Pestana.entries.forEach { pestana ->
            NavigationBarItem(
                selected = pestana == pestanaActual,
                onClick = { onPestanaSeleccionada(pestana) },
                icon = {
                    Icon(painter = painterResource(pestana.icono), contentDescription = null)
                },
                label = { Text(pestana.etiqueta) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = CristalBorde
                )
            )
        }
    }
}
