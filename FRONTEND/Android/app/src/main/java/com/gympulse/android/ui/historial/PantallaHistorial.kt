package com.gympulse.android.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympulse.android.ui.comun.nombreLargo
import com.gympulse.android.ui.componentes.ContenidoSegunEstado
import com.gympulse.android.ui.componentes.RefrescoDeslizable
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.EstadoVacio
import com.gympulse.android.ui.componentes.TarjetaEntrenamiento
import java.time.YearMonth

@Composable
fun PantallaHistorial(
    onVolver: () -> Unit,
    onVerEntrenamiento: (Long) -> Unit,
    viewModel: HistorialViewModel = viewModel(factory = HistorialViewModel.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val refrescando by viewModel.refrescando.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.cargar() }

    EsqueletoPantalla(titulo = "Historial", onVolver = onVolver) { relleno ->
        ContenidoSegunEstado(estado = estado, onReintentar = { viewModel.cargar() }) { lista ->
            val porMes = remember(lista) { lista.groupBy { YearMonth.from(it.fecha) } }

            RefrescoDeslizable(
                refrescando = refrescando,
                onRefrescar = { viewModel.cargar(mostrarIndicador = true) },
                relleno = relleno
            ) {
                LazyColumn(
                    contentPadding = relleno,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (lista.isEmpty()) {
                        item {
                            EstadoVacio(
                                icono = Icons.Rounded.History,
                                titulo = "Sin entrenamientos",
                                descripcion = "Cuando termines tu primer entreno lo verás aquí."
                            )
                        }
                    }

                    porMes.forEach { (mes, entrenamientos) ->
                        item(key = "mes-$mes") {
                            Text(
                                text = "${mes.nombreLargo()} · ${entrenamientos.size}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp)
                            )
                        }
                        items(entrenamientos, key = { it.id }) { entrenamiento ->
                            TarjetaEntrenamiento(
                                entrenamiento = entrenamiento,
                                onClick = { onVerEntrenamiento(entrenamiento.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
