package com.gympulse.android.ui.entrenamiento

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gympulse.android.ui.theme.GymPulseTheme

// Pantalla de entrenamiento. De momento vacia; mas adelante los datos vendran del backend.
@Composable
fun PantallaEntrenamiento(modifier: Modifier = Modifier) {

    // Column apila sus hijos en vertical, uno debajo de otro. Su equivalente horizontal es Row.
    Column(modifier = modifier.padding(24.dp)) {

    }
}

@Preview(showBackground = true)
@Composable
fun PantallaEntrenamientoPreview() {
    GymPulseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PantallaEntrenamiento()
        }
    }
}
