package com.gympulse.android.ui.entrenamientoactivo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import com.gympulse.android.ui.comun.fechaRelativa
import com.gympulse.android.ui.comun.formatearDuracion
import com.gympulse.android.ui.comun.formatearVolumen
import com.gympulse.android.ui.comun.titulo
import com.gympulse.android.ui.comun.volumen
import com.gympulse.android.ui.componentes.ContenidoSegunEstado
import com.gympulse.android.ui.componentes.DialogoConfirmacion
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.EstadoVacio
import com.gympulse.android.ui.componentes.TarjetaDato
import com.gympulse.android.ui.componentes.TarjetaSeries
import com.gympulse.android.ui.componentes.conExtraInferior
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
fun PantallaEntrenamientoActivo(
    onVolver: () -> Unit,
    onTerminado: (Long) -> Unit,
    onDescartado: () -> Unit,
    viewModel: EntrenamientoActivoViewModel = viewModel(factory = EntrenamientoActivoViewModel.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val guardando by viewModel.guardando.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var hojaVisible by rememberSaveable { mutableStateOf(false) }
    var ejercicioHoja by rememberSaveable { mutableStateOf<Long?>(null) }
    var menuAbierto by remember { mutableStateOf(false) }
    var confirmarDescarte by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is EventoActivo.Error -> snackbar.showSnackbar(evento.mensaje)
                is EventoActivo.SerieAnadida -> hojaVisible = false
                is EventoActivo.EjercicioCreado -> ejercicioHoja = evento.ejercicio.id
                is EventoActivo.Terminado -> onTerminado(evento.id)
                EventoActivo.Descartado -> onDescartado()
            }
        }
    }

    val salir = {
        if (viewModel.tieneSeries) onVolver() else confirmarDescarte = true
    }

    BackHandler(onBack = salir)

    if (confirmarDescarte) {
        DialogoConfirmacion(
            titulo = "¿Descartar entrenamiento?",
            texto = if (viewModel.tieneSeries) {
                "Se borrarán el entrenamiento y todas sus series."
            } else {
                "No has registrado ninguna serie. El entrenamiento se eliminará."
            },
            textoConfirmar = "Descartar",
            destructivo = true,
            onConfirmar = {
                confirmarDescarte = false
                viewModel.descartar()
            },
            onCancelar = { confirmarDescarte = false }
        )
    }

    val datos = estado.datosOrNull()

    EsqueletoPantalla(
        titulo = datos?.entrenamiento?.titulo ?: "Entrenamiento",
        onVolver = salir,
        snackbar = snackbar,
        acciones = {
            TextButton(
                onClick = { if (viewModel.tieneSeries) viewModel.terminar() else confirmarDescarte = true },
                enabled = datos != null
            ) {
                Text("Terminar")
            }
            IconButton(onClick = { menuAbierto = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Más opciones")
            }
            DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                DropdownMenuItem(
                    text = { Text("Descartar entrenamiento", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    onClick = {
                        menuAbierto = false
                        confirmarDescarte = true
                    }
                )
            }
        },
        botonFlotante = {
            if (datos != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        ejercicioHoja = null
                        hojaVisible = true
                    },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Añadir ejercicio") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { relleno ->
        ContenidoSegunEstado(estado = estado, onReintentar = viewModel::cargar) { datosListos ->
            ContenidoActivo(
                datos = datosListos,
                inicioMillis = viewModel.inicioMillis,
                relleno = relleno,
                onEliminarSerie = viewModel::eliminarSerie,
                onAnadirSerie = { ejercicioId ->
                    ejercicioHoja = ejercicioId
                    hojaVisible = true
                }
            )

            if (hojaVisible) {
                HojaAnadirSerie(
                    ejercicios = datosListos.ejercicios,
                    seriesActuales = datosListos.entrenamiento.series,
                    ejercicioSeleccionadoId = ejercicioHoja,
                    guardando = guardando,
                    onSeleccionarEjercicio = { ejercicioHoja = it },
                    onCrearEjercicio = { nombre -> viewModel.crearEjercicio(nombre, grupoMuscular = null) },
                    onGuardar = viewModel::anadirSerie,
                    onCerrar = { hojaVisible = false }
                )
            }
        }
    }
}

@Composable
private fun ContenidoActivo(
    datos: DatosEntrenamientoActivo,
    inicioMillis: Long,
    relleno: PaddingValues,
    onEliminarSerie: (Long) -> Unit,
    onAnadirSerie: (Long) -> Unit
) {
    val unidad = LocalUnidadPeso.current
    val entrenamiento = datos.entrenamiento
    val grupos = remember(entrenamiento.series) { entrenamiento.series.agruparPorEjercicio() }
    val esHoy = entrenamiento.fecha == LocalDate.now()

    val ahora by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(1_000)
            value = System.currentTimeMillis()
        }
    }

    LazyColumn(
        contentPadding = relleno.conExtraInferior(80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaDato(
                    etiqueta = if (esHoy) "Tiempo" else "Fecha",
                    valor = if (esHoy) formatearDuracion((ahora - inicioMillis) / 1000) else entrenamiento.fecha.fechaRelativa(),
                    modifier = Modifier.weight(1f)
                )
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
                    titulo = "Añade tu primer ejercicio",
                    descripcion = "Pulsa «Añadir ejercicio» para elegirlo del catálogo y registrar la primera serie."
                )
            }
        } else {
            items(grupos, key = { it.ejercicioId }) { grupo ->
                TarjetaSeries(
                    grupo = grupo,
                    onEliminarSerie = onEliminarSerie,
                    onAnadirSerie = { onAnadirSerie(grupo.ejercicioId) }
                )
            }
        }
    }
}
