package com.gympulse.android.ui.ejercicios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.gympulse.android.data.modelo.Ejercicio
import com.gympulse.android.ui.componentes.BotonPrincipal
import com.gympulse.android.ui.componentes.BotonSecundario
import com.gympulse.android.ui.componentes.CampoTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaNuevoEjercicio(
    guardando: Boolean,
    onGuardar: (nombre: String, grupo: String?, descripcion: String) -> Unit,
    onCerrar: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var grupo by rememberSaveable { mutableStateOf<String?>(null) }
    var descripcion by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            Text("Nuevo ejercicio", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            CampoTexto(valor = nombre, onCambio = { nombre = it }, etiqueta = "Nombre")
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Grupo muscular",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GRUPOS_MUSCULARES.forEach { opcion ->
                    FilterChip(
                        selected = grupo == opcion,
                        onClick = { grupo = if (grupo == opcion) null else opcion },
                        label = { Text(opcion) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            CampoTexto(
                valor = descripcion,
                onCambio = { descripcion = it },
                etiqueta = "Descripción (opcional)",
                lineaUnica = false,
                accionIme = ImeAction.Default
            )
            Spacer(Modifier.height(24.dp))
            BotonPrincipal(
                texto = "Guardar ejercicio",
                onClick = { onGuardar(nombre, grupo, descripcion) },
                habilitado = nombre.isNotBlank(),
                cargando = guardando
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaDetalleEjercicio(
    ejercicio: Ejercicio,
    onVerProgresion: () -> Unit,
    onEliminar: () -> Unit,
    onCerrar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            Text(ejercicio.nombre, style = MaterialTheme.typography.headlineSmall)
            ejercicio.grupoMuscular?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = ejercicio.descripcion?.takeIf { it.isNotBlank() } ?: "Sin descripción.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonSecundario(
                    texto = "Ver progresión",
                    onClick = onVerProgresion,
                    icono = Icons.AutoMirrored.Rounded.ShowChart,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = onEliminar,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Eliminar ejercicio",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
