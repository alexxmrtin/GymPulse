package com.gympulse.android.ui.inicio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gympulse.android.ui.theme.GymPulseTheme

// Pantalla principal de la app. De momento con datos escritos a mano; mas adelante vendran del backend.
@Composable
fun PantallaInicio(modifier: Modifier = Modifier) {

    // KOTLIN: listOf() crea una lista inmutable. El tipo (List<String>) lo deduce solo el compilador, no hace falta declararlo.
    val ejercicios = listOf(
        "Press de banca",
        "Sentadilla",
        "Dominadas",
        "Press militar",
        "Curl de biceps"
    )

    // Column apila sus hijos en vertical, uno debajo de otro. Su equivalente horizontal es Row.
    Column(modifier = modifier.padding(24.dp)) {

        // style saca el tamano y grosor del tema, en vez de fijar numeros a mano. Asi toda la app es coherente.
        Text(
            text = "GymPulse",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Tus ejercicios",
            style = MaterialTheme.typography.bodyMedium
        )

        // Spacer es un hueco vacio. Los dp son unidades que se ven igual en cualquier pantalla.
        Spacer(modifier = Modifier.height(24.dp))

        // KOTLIN: forEach recorre la lista y ejecuta el bloque con cada elemento. Dentro del bloque, "it" es el elemento actual cuando no le pones nombre.
        ejercicios.forEach { nombre ->
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

// Vista previa: pinta la pantalla en Android Studio sin arrancar la app.
// Surface pinta el fondo del tema. Hace falta aqui porque la vista previa no pasa por el Scaffold de MainActivity, que es quien pone el fondo negro en la app real.
@Preview(showBackground = true)
@Composable
fun PantallaInicioPreview() {
    GymPulseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PantallaInicio()
        }
    }
}
