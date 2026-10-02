package com.gympulse.android.ui.entrenamiento

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympulse.android.ui.comun.titulo
import com.gympulse.android.ui.componentes.BotonPrincipal
import com.gympulse.android.ui.componentes.ContenidoSegunEstado
import com.gympulse.android.ui.componentes.RefrescoDeslizable
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.EstadoVacio
import com.gympulse.android.ui.componentes.Tarjeta
import com.gympulse.android.ui.componentes.TarjetaEntrenamiento
import com.gympulse.android.ui.componentes.TituloSeccion

@Composable
fun PantallaEntrenamiento(
    onEntrenamientoEmpezado: (Long) -> Unit,
    onContinuar: (Long) -> Unit,
    onEjercicios: () -> Unit,
    onHistorial: () -> Unit,
    onVerEntrenamiento: (Long) -> Unit,
    viewModel: EntrenamientoViewModel = viewModel(factory = EntrenamientoViewModel.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val refrescando by viewModel.refrescando.collectAsStateWithLifecycle()
    val empezando by viewModel.empezando.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.cargar() }

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is EventoEntrenamiento.Empezado -> onEntrenamientoEmpezado(evento.id)
                is EventoEntrenamiento.Error -> snackbar.showSnackbar(evento.mensaje)
            }
        }
    }

    if (mostrarDialogo) {
        DialogoNuevoEntrenamiento(
            onEmpezar = { nombre ->
                mostrarDialogo = false
                viewModel.empezar(nombre)
            },
            onCancelar = { mostrarDialogo = false }
        )
    }

    EsqueletoPantalla(titulo = "Entrenamiento", snackbar = snackbar) { relleno ->
        ContenidoSegunEstado(estado = estado, onReintentar = { viewModel.cargar() }) { resumen ->
            RefrescoDeslizable(
                refrescando = refrescando,
                onRefrescar = { viewModel.cargar(mostrarIndicador = true) },
                relleno = relleno
            ) {
                ContenidoEntrenamiento(
                    resumen = resumen,
                    relleno = relleno,
                    empezando = empezando,
                    onEmpezar = { mostrarDialogo = true },
                    onContinuar = onContinuar,
                    onEjercicios = onEjercicios,
                    onHistorial = onHistorial,
                    onVerEntrenamiento = onVerEntrenamiento
                )
            }
        }
    }
}

@Composable
private fun ContenidoEntrenamiento(
    resumen: ResumenEntrenamiento,
    relleno: PaddingValues,
    empezando: Boolean,
    onEmpezar: () -> Unit,
    onContinuar: (Long) -> Unit,
    onEjercicios: () -> Unit,
    onHistorial: () -> Unit,
    onVerEntrenamiento: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = relleno,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            TarjetaEmpezar(
                entrenosSemana = resumen.estaSemana,
                empezando = empezando,
                onEmpezar = onEmpezar
            )
        }

        resumen.deHoy?.let { deHoy ->
            item {
                Tarjeta(onClick = { onContinuar(deHoy.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconoRedondo(Icons.Rounded.Timer)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Entreno de hoy", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${deHoy.titulo} · ${deHoy.series.size} series",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("Continuar", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccesoRapido(
                    icono = Icons.Rounded.FitnessCenter,
                    titulo = "Ejercicios",
                    subtitulo = "Catálogo",
                    onClick = onEjercicios,
                    modifier = Modifier.weight(1f)
                )
                AccesoRapido(
                    icono = Icons.Rounded.History,
                    titulo = "Historial",
                    subtitulo = "Tus sesiones",
                    onClick = onHistorial,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            TituloSeccion("Recientes")
        }

        if (resumen.recientes.isEmpty()) {
            item {
                EstadoVacio(
                    icono = Icons.Rounded.FitnessCenter,
                    titulo = "Aún no hay entrenos",
                    descripcion = "Empieza tu primer entrenamiento y aparecerá aquí."
                )
            }
        } else {
            items(resumen.recientes, key = { it.id }) { entrenamiento ->
                TarjetaEntrenamiento(
                    entrenamiento = entrenamiento,
                    onClick = { onVerEntrenamiento(entrenamiento.id) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaEmpezar(
    entrenosSemana: Int,
    empezando: Boolean,
    onEmpezar: () -> Unit
) {
    val degradado = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceContainer)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(degradado)
            .padding(20.dp)
    ) {
        Text("¿Listo para entrenar?", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            text = when (entrenosSemana) {
                0 -> "Todavía no has entrenado esta semana"
                1 -> "Llevas 1 entreno esta semana"
                else -> "Llevas $entrenosSemana entrenos esta semana"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        BotonPrincipal(
            texto = "Empezar entrenamiento",
            onClick = onEmpezar,
            cargando = empezando,
            icono = Icons.Rounded.PlayArrow
        )
    }
}

@Composable
private fun AccesoRapido(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Tarjeta(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoRedondo(icono)
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Text(
            text = subtitulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IconoRedondo(icono: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
    }
}
