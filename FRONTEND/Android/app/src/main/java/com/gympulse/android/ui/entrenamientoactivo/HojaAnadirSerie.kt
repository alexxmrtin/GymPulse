package com.gympulse.android.ui.entrenamientoactivo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gympulse.android.data.modelo.Ejercicio
import com.gympulse.android.data.modelo.Serie
import com.gympulse.android.data.preferencias.UnidadPeso
import com.gympulse.android.ui.comun.LocalUnidadPeso
import com.gympulse.android.ui.comun.numeroEditable
import com.gympulse.android.ui.comun.parsearDecimal
import com.gympulse.android.ui.componentes.BotonPrincipal
import com.gympulse.android.ui.componentes.CampoTexto
import com.gympulse.android.ui.componentes.SelectorNumerico

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaAnadirSerie(
    ejercicios: List<Ejercicio>,
    seriesActuales: List<Serie>,
    ejercicioSeleccionadoId: Long?,
    guardando: Boolean,
    onSeleccionarEjercicio: (Long?) -> Unit,
    onCrearEjercicio: (String) -> Unit,
    onGuardar: (ejercicioId: Long, repeticiones: Int, pesoKg: Double) -> Unit,
    onCerrar: () -> Unit
) {
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val seleccionado = ejercicios.firstOrNull { it.id == ejercicioSeleccionadoId }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja,
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
            if (seleccionado == null) {
                SelectorEjercicio(
                    ejercicios = ejercicios,
                    onSeleccionar = { onSeleccionarEjercicio(it.id) },
                    onCrear = onCrearEjercicio
                )
            } else {
                FormularioSerie(
                    ejercicio = seleccionado,
                    ultimaSerie = seriesActuales.lastOrNull { it.ejercicioId == seleccionado.id },
                    guardando = guardando,
                    onCambiarEjercicio = { onSeleccionarEjercicio(null) },
                    onGuardar = { repeticiones, pesoKg -> onGuardar(seleccionado.id, repeticiones, pesoKg) }
                )
            }
        }
    }
}

@Composable
private fun SelectorEjercicio(
    ejercicios: List<Ejercicio>,
    onSeleccionar: (Ejercicio) -> Unit,
    onCrear: (String) -> Unit
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    val filtrados = remember(ejercicios, busqueda) {
        ejercicios.filter { it.nombre.contains(busqueda.trim(), ignoreCase = true) }
    }
    val existeExacto = ejercicios.any { it.nombre.equals(busqueda.trim(), ignoreCase = true) }

    Text("Elige un ejercicio", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(16.dp))
    CampoTexto(
        valor = busqueda,
        onCambio = { busqueda = it },
        etiqueta = "Buscar",
        icono = Icons.Rounded.Search
    )
    Spacer(Modifier.height(8.dp))

    LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
        if (busqueda.isNotBlank() && !existeExacto) {
            item {
                TextButton(onClick = { onCrear(busqueda) }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Crear «${busqueda.trim()}»")
                }
            }
        }
        items(filtrados, key = { it.id }) { ejercicio ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeleccionar(ejercicio) }
                    .padding(vertical = 12.dp, horizontal = 4.dp)
            ) {
                Text(ejercicio.nombre, style = MaterialTheme.typography.bodyLarge)
                ejercicio.grupoMuscular?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (filtrados.isEmpty() && busqueda.isBlank()) {
            item {
                Text(
                    text = "El catálogo está vacío. Escribe un nombre para crear tu primer ejercicio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun FormularioSerie(
    ejercicio: Ejercicio,
    ultimaSerie: Serie?,
    guardando: Boolean,
    onCambiarEjercicio: () -> Unit,
    onGuardar: (repeticiones: Int, pesoKg: Double) -> Unit
) {
    val unidad = LocalUnidadPeso.current
    var repeticiones by rememberSaveable(ejercicio.id) { mutableStateOf(ultimaSerie?.repeticiones?.toString() ?: "10") }
    var peso by rememberSaveable(ejercicio.id) {
        mutableStateOf(ultimaSerie?.let { numeroEditable(unidad.desdeKg(it.peso)) } ?: "")
    }

    val repsValidas = repeticiones.toIntOrNull()?.takeIf { it > 0 }
    val pesoValido = parsearDecimal(peso.ifBlank { "0" })?.takeIf { it >= 0 }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                text = ejercicio.nombre,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (ultimaSerie != null) "Rellenado con tu última serie" else "Nueva serie",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onCambiarEjercicio) { Text("Cambiar") }
    }

    Spacer(Modifier.height(20.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectorNumerico(
            etiqueta = "Repeticiones",
            valor = repeticiones,
            onCambio = { repeticiones = it },
            paso = 1.0,
            modifier = Modifier.weight(1f)
        )
        SelectorNumerico(
            etiqueta = "Peso",
            valor = peso,
            onCambio = { peso = it },
            paso = if (unidad == UnidadPeso.KG) 2.5 else 5.0,
            sufijo = unidad.simbolo,
            decimales = true,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(Modifier.height(24.dp))

    BotonPrincipal(
        texto = "Guardar serie",
        onClick = {
            if (repsValidas != null && pesoValido != null) onGuardar(repsValidas, unidad.aKg(pesoValido))
        },
        habilitado = repsValidas != null && pesoValido != null,
        cargando = guardando
    )
}
