package com.gympulse.android.ui.detalleentrenamiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympulse.android.ui.comun.LocalUnidadPeso
import com.gympulse.android.ui.comun.agruparPorEjercicio
import com.gympulse.android.ui.comun.datosOrNull
import com.gympulse.android.ui.comun.fechaLarga
import com.gympulse.android.ui.comun.formatearVolumen
import com.gympulse.android.ui.comun.numeroEjercicios
import com.gympulse.android.ui.comun.titulo
import com.gympulse.android.ui.comun.volumen
import com.gympulse.android.ui.componentes.BotonSecundario
import com.gympulse.android.ui.componentes.ContenidoSegunEstado
import com.gympulse.android.ui.componentes.DialogoConfirmacion
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.EstadoVacio
import com.gympulse.android.ui.componentes.TarjetaDato
import com.gympulse.android.ui.componentes.TarjetaSeries

@Composable
fun PantallaDetalleEntrenamiento(
    onVolver: () -> Unit,
    onAnadirSeries: (Long) -> Unit,
    onEliminado: () -> Unit,
    viewModel: DetalleEntrenamientoViewModel = viewModel(factory = DetalleEntrenamientoViewModel.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmarBorrado by rememberSaveable { mutableStateOf(false) }
    val unidad = LocalUnidadPeso.current

    LaunchedEffect(Unit) { viewModel.cargar() }

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                EventoDetalle.Eliminado -> onEliminado()
                is EventoDetalle.Error -> snackbar.showSnackbar(evento.mensaje)
            }
        }
    }

    if (confirmarBorrado) {
        DialogoConfirmacion(
            titulo = "¿Eliminar entrenamiento?",
            texto = "Se borrarán el entrenamiento y todas sus series. No se puede deshacer.",
            textoConfirmar = "Eliminar",
            destructivo = true,
            onConfirmar = {
                confirmarBorrado = false
                viewModel.eliminar()
            },
            onCancelar = { confirmarBorrado = false }
        )
    }

    EsqueletoPantalla(
        titulo = estado.datosOrNull()?.titulo ?: "Entrenamiento",
        onVolver = onVolver,
        snackbar = snackbar,
        acciones = {
            IconButton(onClick = { confirmarBorrado = true }, enabled = estado.datosOrNull() != null) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Eliminar entrenamiento")
            }
        }
    ) { relleno ->
        ContenidoSegunEstado(estado = estado, onReintentar = viewModel::cargar) { entrenamiento ->
            val grupos = remember(entrenamiento) { entrenamiento.series.agruparPorEjercicio() }

            LazyColumn(
                contentPadding = relleno,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = entrenamiento.fecha.fechaLarga(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TarjetaDato(etiqueta = "Ejercicios", valor = "${entrenamiento.numeroEjercicios}", modifier = Modifier.weight(1f))
                        TarjetaDato(etiqueta = "Series", valor = "${entrenamiento.series.size}", modifier = Modifier.weight(1f))
                        TarjetaDato(
                            etiqueta = "Volumen",
                            valor = formatearVolumen(entrenamiento.volumen, unidad),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (grupos.isEmpty()) {
                    item {
                        EstadoVacio(
                            icono = Icons.Rounded.FitnessCenter,
                            titulo = "Sin series",
                            descripcion = "Este entrenamiento no tiene series registradas."
                        )
                    }
                } else {
                    items(grupos, key = { it.ejercicioId }) { grupo ->
                        TarjetaSeries(grupo = grupo)
                    }
                }

                item {
                    BotonSecundario(
                        texto = "Añadir series",
                        onClick = { onAnadirSeries(entrenamiento.id) },
                        icono = Icons.Rounded.Add,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
