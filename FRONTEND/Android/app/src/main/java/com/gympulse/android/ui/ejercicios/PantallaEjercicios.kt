package com.gympulse.android.ui.ejercicios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympulse.android.data.modelo.Ejercicio
import com.gympulse.android.ui.componentes.CampoTexto
import com.gympulse.android.ui.componentes.ContenidoSegunEstado
import com.gympulse.android.ui.componentes.DialogoConfirmacion
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.EstadoVacio
import com.gympulse.android.ui.componentes.Tarjeta
import com.gympulse.android.ui.componentes.conExtraInferior

@Composable
fun PantallaEjercicios(
    onVolver: () -> Unit,
    onVerProgresion: (Long) -> Unit,
    viewModel: EjerciciosViewModel = viewModel(factory = EjerciciosViewModel.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()
    val guardando by viewModel.guardando.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var creando by rememberSaveable { mutableStateOf(false) }
    var detalleId by rememberSaveable { mutableStateOf<Long?>(null) }
    var eliminarId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is EventoEjercicios.Mensaje -> snackbar.showSnackbar(evento.texto)
                EventoEjercicios.Creado -> creando = false
                EventoEjercicios.Eliminado -> detalleId = null
            }
        }
    }

    EsqueletoPantalla(
        titulo = "Ejercicios",
        onVolver = onVolver,
        snackbar = snackbar,
        botonFlotante = {
            ExtendedFloatingActionButton(
                onClick = { creando = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Nuevo") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { relleno ->
        ContenidoSegunEstado(estado = estado, onReintentar = viewModel::cargar) { lista ->
            ListaEjercicios(
                lista = lista,
                filtro = filtro,
                relleno = relleno,
                onBusqueda = viewModel::onBusqueda,
                onGrupo = viewModel::onGrupo,
                onSeleccionar = { detalleId = it.id }
            )

            lista.firstOrNull { it.id == detalleId }?.let { ejercicio ->
                HojaDetalleEjercicio(
                    ejercicio = ejercicio,
                    onVerProgresion = {
                        detalleId = null
                        onVerProgresion(ejercicio.id)
                    },
                    onEliminar = { eliminarId = ejercicio.id },
                    onCerrar = { detalleId = null }
                )
            }

            lista.firstOrNull { it.id == eliminarId }?.let { ejercicio ->
                DialogoConfirmacion(
                    titulo = "¿Eliminar «${ejercicio.nombre}»?",
                    texto = "Desaparecerá del catálogo. Solo se puede borrar si no tiene series registradas.",
                    textoConfirmar = "Eliminar",
                    destructivo = true,
                    onConfirmar = {
                        eliminarId = null
                        viewModel.eliminar(ejercicio.id)
                    },
                    onCancelar = { eliminarId = null }
                )
            }
        }
    }

    if (creando) {
        HojaNuevoEjercicio(
            guardando = guardando,
            onGuardar = viewModel::crear,
            onCerrar = { creando = false }
        )
    }
}

@Composable
private fun ListaEjercicios(
    lista: List<Ejercicio>,
    filtro: FiltroEjercicios,
    relleno: PaddingValues,
    onBusqueda: (String) -> Unit,
    onGrupo: (String) -> Unit,
    onSeleccionar: (Ejercicio) -> Unit
) {
    val grupos = remember(lista) { lista.mapNotNull { it.grupoMuscular }.distinct().sorted() }
    val visibles = remember(lista, filtro) {
        lista.filter { ejercicio ->
            ejercicio.nombre.contains(filtro.busqueda.trim(), ignoreCase = true) &&
                (filtro.grupo == null || ejercicio.grupoMuscular == filtro.grupo)
        }
    }

    LazyColumn(
        contentPadding = relleno.conExtraInferior(72.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CampoTexto(
                valor = filtro.busqueda,
                onCambio = onBusqueda,
                etiqueta = "Buscar ejercicio",
                icono = Icons.Rounded.Search
            )
        }

        if (grupos.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(grupos) { grupo ->
                        FilterChip(
                            selected = filtro.grupo == grupo,
                            onClick = { onGrupo(grupo) },
                            label = { Text(grupo) }
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(4.dp)) }

        when {
            lista.isEmpty() -> item {
                EstadoVacio(
                    icono = Icons.Rounded.FitnessCenter,
                    titulo = "Catálogo vacío",
                    descripcion = "Crea tus ejercicios con el botón «Nuevo» para poder usarlos en tus entrenos."
                )
            }

            visibles.isEmpty() -> item {
                EstadoVacio(
                    icono = Icons.Rounded.SearchOff,
                    titulo = "Sin resultados",
                    descripcion = "Ningún ejercicio coincide con la búsqueda."
                )
            }

            else -> items(visibles, key = { it.id }) { ejercicio ->
                FilaEjercicio(ejercicio = ejercicio, onClick = { onSeleccionar(ejercicio) })
            }
        }
    }
}

@Composable
private fun FilaEjercicio(ejercicio: Ejercicio, onClick: () -> Unit) {
    Tarjeta(onClick = onClick, relleno = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = ejercicio.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val detalle = listOfNotNull(ejercicio.grupoMuscular, ejercicio.descripcion?.takeIf { it.isNotBlank() })
                if (detalle.isNotEmpty()) {
                    Text(
                        text = detalle.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
