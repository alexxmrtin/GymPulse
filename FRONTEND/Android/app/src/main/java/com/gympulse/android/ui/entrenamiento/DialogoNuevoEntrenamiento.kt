package com.gympulse.android.ui.entrenamiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.gympulse.android.ui.componentes.CampoTexto

private val SUGERENCIAS = listOf("Torso", "Pierna", "Push", "Pull", "Full body")

@Composable
fun DialogoNuevoEntrenamiento(
    onEmpezar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("Nuevo entrenamiento") },
        text = {
            Column {
                Text(
                    text = "Ponle nombre si quieres. Podrás añadir ejercicios y series sobre la marcha.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                CampoTexto(
                    valor = nombre,
                    onCambio = { nombre = it.take(60) },
                    etiqueta = "Nombre (opcional)",
                    accionIme = ImeAction.Done,
                    alAccionIme = { onEmpezar(nombre) }
                )
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SUGERENCIAS.forEach { sugerencia ->
                        FilterChip(
                            selected = nombre == sugerencia,
                            onClick = { nombre = sugerencia },
                            label = { Text(sugerencia) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onEmpezar(nombre) }) { Text("Empezar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
