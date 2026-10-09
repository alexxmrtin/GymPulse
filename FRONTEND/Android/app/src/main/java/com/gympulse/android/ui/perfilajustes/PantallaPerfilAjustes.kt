package com.gympulse.android.ui.perfilajustes

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxWidth
import com.gympulse.android.ui.comun.edadDesde
import com.gympulse.android.ui.comun.fechaMedia
import com.gympulse.android.ui.componentes.BotonPrincipal
import com.gympulse.android.ui.componentes.CampoTexto
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.IndicadorCarga
import com.gympulse.android.ui.componentes.MensajeError
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun PantallaPerfilAjustes(
    onVolver: () -> Unit,
    viewModel: PerfilAjustesViewModel = viewModel(factory = PerfilAjustesViewModel.Fabrica)
) {
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                EventoPerfilAjustes.Guardado -> onVolver()
                is EventoPerfilAjustes.Error -> snackbar.showSnackbar(evento.mensaje)
            }
        }
    }

    if (mostrarCalendario) {
        SelectorFecha(
            inicial = formulario.fechaNacimiento,
            onElegir = { fecha ->
                mostrarCalendario = false
                viewModel.onFechaNacimiento(fecha)
            },
            onCancelar = { mostrarCalendario = false }
        )
    }

    EsqueletoPantalla(titulo = "Datos personales", onVolver = onVolver, snackbar = snackbar) { relleno ->
        when {
            formulario.cargando -> IndicadorCarga()
            formulario.errorCarga != null -> MensajeError(formulario.errorCarga.orEmpty(), onReintentar = viewModel::cargar)
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(relleno),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Se usan para calcular tu progreso. Todos son opcionales.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                CampoTexto(
                    valor = formulario.peso,
                    onCambio = viewModel::onPeso,
                    etiqueta = "Peso",
                    icono = Icons.Rounded.MonitorWeight,
                    sufijo = formulario.unidad.simbolo,
                    teclado = KeyboardType.Decimal,
                    error = formulario.errorPeso
                )
                CampoTexto(
                    valor = formulario.altura,
                    onCambio = viewModel::onAltura,
                    etiqueta = "Altura",
                    icono = Icons.Rounded.Height,
                    sufijo = "cm",
                    teclado = KeyboardType.Number,
                    error = formulario.errorAltura
                )
                CampoFecha(
                    fecha = formulario.fechaNacimiento,
                    onAbrir = { mostrarCalendario = true },
                    onBorrar = { viewModel.onFechaNacimiento(null) }
                )
                Spacer(Modifier.height(12.dp))
                BotonPrincipal(
                    texto = "Guardar cambios",
                    onClick = viewModel::guardar,
                    habilitado = formulario.puedeGuardar,
                    cargando = formulario.guardando
                )
            }
        }
    }
}

@Composable
private fun CampoFecha(
    fecha: LocalDate?,
    onAbrir: () -> Unit,
    onBorrar: () -> Unit
) {
    val interacciones = remember { MutableInteractionSource() }

    LaunchedEffect(interacciones) {
        interacciones.interactions.collect { interaccion ->
            if (interaccion is PressInteraction.Release) onAbrir()
        }
    }

    OutlinedTextField(
        value = fecha?.let { "${it.fechaMedia()} · ${edadDesde(it)} años" }.orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text("Fecha de nacimiento") },
        leadingIcon = { Icon(Icons.Rounded.Cake, contentDescription = null) },
        trailingIcon = if (fecha != null) {
            {
                IconButton(onClick = onBorrar) {
                    Icon(Icons.Rounded.Clear, contentDescription = "Borrar fecha")
                }
            }
        } else null,
        interactionSource = interacciones,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFecha(
    inicial: LocalDate?,
    onElegir: (LocalDate) -> Unit,
    onCancelar: () -> Unit
) {
    val hoy = LocalDate.now()
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = (inicial ?: hoy.minusYears(25)).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        yearRange = (hoy.year - 100)..hoy.year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isBefore(hoy)
        }
    )

    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(
                onClick = {
                    estado.selectedDateMillis?.let { millis ->
                        onElegir(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
                enabled = estado.selectedDateMillis != null
            ) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = estado)
    }
}
