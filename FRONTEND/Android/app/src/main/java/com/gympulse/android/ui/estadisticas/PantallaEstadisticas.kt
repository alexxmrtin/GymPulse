package com.gympulse.android.ui.estadisticas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympulse.android.ui.comun.LocalUnidadPeso
import com.gympulse.android.ui.comun.fechaCorta
import com.gympulse.android.ui.comun.fechaMedia
import com.gympulse.android.ui.comun.formatearNumero
import com.gympulse.android.ui.comun.formatearPeso
import com.gympulse.android.ui.comun.formatearVolumen
import com.gympulse.android.ui.componentes.ContenidoSegunEstado
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.EstadoVacio
import com.gympulse.android.ui.componentes.GraficaBarras
import com.gympulse.android.ui.componentes.GraficaLinea
import com.gympulse.android.ui.componentes.Tarjeta
import com.gympulse.android.ui.componentes.TarjetaDato
import com.gympulse.android.ui.componentes.TituloSeccion

@Composable
fun PantallaEstadisticas(
    onVolver: () -> Unit,
    viewModel: EstadisticasViewModel = viewModel(factory = EstadisticasViewModel.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val seleccionado by viewModel.ejercicioSeleccionado.collectAsStateWithLifecycle()

    EsqueletoPantalla(titulo = "Estadísticas", onVolver = onVolver) { relleno ->
        ContenidoSegunEstado(estado = estado, onReintentar = viewModel::cargar) { datos ->
            ContenidoEstadisticas(
                datos = datos,
                seleccionado = seleccionado,
                relleno = relleno,
                onSeleccionar = viewModel::seleccionarEjercicio
            )
        }
    }
}

@Composable
private fun ContenidoEstadisticas(
    datos: DatosEstadisticas,
    seleccionado: Long?,
    relleno: PaddingValues,
    onSeleccionar: (Long) -> Unit
) {
    val unidad = LocalUnidadPeso.current
    val resumen = datos.resumen
    val ejercicioActivo = resumen.ejercicios.firstOrNull { it.id == seleccionado } ?: resumen.ejercicios.firstOrNull()
    val progresion = remember(datos, ejercicioActivo) {
        ejercicioActivo?.let { calcularProgresion(datos.entrenamientos, it) }
    }

    LazyColumn(
        contentPadding = relleno,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (resumen.totalEntrenos == 0) {
            item {
                EstadoVacio(
                    icono = Icons.AutoMirrored.Rounded.ShowChart,
                    titulo = "Aún no hay datos",
                    descripcion = "Registra algunos entrenamientos y aquí verás tu evolución."
                )
            }
            return@LazyColumn
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaDato(etiqueta = "Entrenos", valor = "${resumen.totalEntrenos}", modifier = Modifier.weight(1f))
                TarjetaDato(etiqueta = "Últimos 30 días", valor = "${resumen.ultimos30Dias}", modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaDato(etiqueta = "Series", valor = "${resumen.totalSeries}", modifier = Modifier.weight(1f))
                TarjetaDato(
                    etiqueta = "Volumen total",
                    valor = formatearVolumen(resumen.volumenTotal, unidad),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            TituloSeccion("Entrenos por semana")
            Tarjeta {
                GraficaBarras(
                    valores = resumen.semanas.map { it.second },
                    etiquetas = resumen.semanas.map { it.first.fechaCorta() },
                    describirSeleccion = { indice ->
                        val (inicio, cantidad) = resumen.semanas[indice]
                        "Semana del ${inicio.fechaCorta()}: $cantidad ${if (cantidad == 1) "entreno" else "entrenos"}"
                    }
                )
            }
        }

        if (ejercicioActivo != null && progresion != null) {
            item {
                Spacer(Modifier.height(4.dp))
                TituloSeccion("Progresión por ejercicio")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(resumen.ejercicios, key = { it.id }) { ejercicio ->
                        FilterChip(
                            selected = ejercicio.id == ejercicioActivo.id,
                            onClick = { onSeleccionar(ejercicio.id) },
                            label = { Text(ejercicio.nombre) }
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaDato(
                        etiqueta = "Récord",
                        valor = formatearPeso(progresion.record, unidad),
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaDato(
                        etiqueta = "Series",
                        valor = "${ejercicioActivo.series}",
                        detalle = "en ${progresion.puntos.size} sesiones",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Tarjeta {
                    Text("Peso máximo por sesión", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    if (progresion.puntos.size < 2) {
                        Text(
                            text = "Necesitas al menos dos sesiones con este ejercicio para ver la evolución.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        GraficaLinea(
                            valores = progresion.puntos.map { unidad.desdeKg(it.pesoMaximo) },
                            etiquetaInicio = progresion.puntos.first().fecha.fechaCorta(),
                            etiquetaFin = progresion.puntos.last().fecha.fechaCorta(),
                            formatearEje = { formatearNumero(it, 0) },
                            describirSeleccion = { indice ->
                                val punto = progresion.puntos[indice]
                                "${punto.fecha.fechaMedia()}: ${formatearPeso(punto.pesoMaximo, unidad)}"
                            }
                        )
                    }
                }
            }
        }
    }
}
