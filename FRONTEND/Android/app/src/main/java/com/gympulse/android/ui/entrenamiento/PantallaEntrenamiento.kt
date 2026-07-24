package com.gympulse.android.ui.entrenamiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gympulse.android.ui.componentes.SeparadorDifuminado
import com.gympulse.android.ui.theme.GymPulseTheme

@Composable
fun PantallaEntrenamiento(
    modifier: Modifier = Modifier,
    onEjercicios: () -> Unit = {},
    onEntrenamientoVacio: () -> Unit = {},
    onCrearRutina: () -> Unit = {}
) {
    Column(modifier = modifier.padding(24.dp)) {

        Text(
            text = "Entrenamiento",
            style = MaterialTheme.typography.headlineLarge
        )

        SeparadorDifuminado()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onEjercicios,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Ejercicios")
            }

            Button(
                onClick = onEntrenamientoVacio,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Entrenamiento vacio",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Rutinas",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onCrearRutina,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear rutina")
        }
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
