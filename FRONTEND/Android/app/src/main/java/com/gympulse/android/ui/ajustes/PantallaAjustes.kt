package com.gympulse.android.ui.ajustes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Pantalla vacia de momento, solo para que la barra inferior tenga a donde navegar.
@Composable
fun PantallaAjustes(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(24.dp)) {
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineLarge
        )
    }
}
