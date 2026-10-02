package com.gympulse.android.ui.perfil

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympulse.android.data.perfil.urlFoto
import com.gympulse.android.ui.comun.LocalUnidadPeso
import com.gympulse.android.ui.comun.edadDesde
import com.gympulse.android.ui.comun.formatearNumero
import com.gympulse.android.ui.comun.formatearPeso
import com.gympulse.android.ui.comun.rachaSemanas
import com.gympulse.android.ui.componentes.BotonSecundario
import com.gympulse.android.ui.componentes.ContenidoSegunEstado
import com.gympulse.android.ui.componentes.RefrescoDeslizable
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.Tarjeta
import com.gympulse.android.ui.componentes.TarjetaDato
import com.gympulse.android.ui.componentes.TarjetaEntrenamiento
import com.gympulse.android.ui.componentes.TituloSeccion
import java.time.YearMonth

@Composable
fun PantallaPerfil(
    onEditarPerfil: () -> Unit,
    onEstadisticas: () -> Unit,
    onHistorial: () -> Unit,
    onVerEntrenamiento: (Long) -> Unit,
    viewModel: PerfilViewModel = viewModel(factory = PerfilViewModel.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val refrescando by viewModel.refrescando.collectAsStateWithLifecycle()
    val mes by viewModel.mes.collectAsStateWithLifecycle()
    val subiendoFoto by viewModel.subiendoFoto.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.cargar() }

    LaunchedEffect(viewModel) {
        viewModel.errores.collect { snackbar.showSnackbar(it) }
    }

    EsqueletoPantalla(
        titulo = "Perfil",
        snackbar = snackbar,
        acciones = {
            IconButton(onClick = onEditarPerfil) {
                Icon(Icons.Rounded.Edit, contentDescription = "Editar perfil")
            }
        }
    ) { relleno ->
        ContenidoSegunEstado(estado = estado, onReintentar = { viewModel.cargar() }) { datos ->
            RefrescoDeslizable(
                refrescando = refrescando,
                onRefrescar = { viewModel.cargar(mostrarIndicador = true) },
                relleno = relleno
            ) {
                ContenidoPerfil(
                    datos = datos,
                    subiendoFoto = subiendoFoto,
                    mes = mes,
                    relleno = relleno,
                    onElegirFoto = viewModel::cambiarFoto,
                    onQuitarFoto = viewModel::quitarFoto,
                    onMesAnterior = viewModel::mesAnterior,
                    onMesSiguiente = viewModel::mesSiguiente,
                    onEstadisticas = onEstadisticas,
                    onHistorial = onHistorial,
                    onVerEntrenamiento = onVerEntrenamiento
                )
            }
        }
    }
}

@Composable
private fun ContenidoPerfil(
    datos: DatosPerfil,
    subiendoFoto: Boolean,
    mes: YearMonth,
    relleno: PaddingValues,
    onElegirFoto: (Uri) -> Unit,
    onQuitarFoto: () -> Unit,
    onMesAnterior: () -> Unit,
    onMesSiguiente: () -> Unit,
    onEstadisticas: () -> Unit,
    onHistorial: () -> Unit,
    onVerEntrenamiento: (Long) -> Unit
) {
    val unidad = LocalUnidadPeso.current
    val usuario = datos.usuario
    val lista = datos.entrenamientos

    val diasDelMes = remember(lista, mes) {
        lista.filter { YearMonth.from(it.fecha) == mes }.map { it.fecha.dayOfMonth }.toSet()
    }
    val esteMes = remember(lista) {
        lista.count { YearMonth.from(it.fecha) == YearMonth.now() }
    }
    val racha = remember(lista) { lista.rachaSemanas() }

    LazyColumn(
        contentPadding = relleno,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarEditable(
                    nombre = usuario.username,
                    urlFoto = usuario.urlFoto(),
                    subiendo = subiendoFoto,
                    onElegirFoto = onElegirFoto,
                    onQuitarFoto = onQuitarFoto
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = usuario.username,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = usuario.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaDato(
                    etiqueta = "Peso",
                    valor = usuario.peso?.let { formatearPeso(it, unidad) } ?: "—",
                    modifier = Modifier.weight(1f)
                )
                TarjetaDato(
                    etiqueta = "Altura",
                    valor = usuario.altura?.let { "${formatearNumero(it, 0)} cm" } ?: "—",
                    modifier = Modifier.weight(1f)
                )
                TarjetaDato(
                    etiqueta = "Edad",
                    valor = usuario.fechaNacimiento?.let { "${edadDesde(it)} años" } ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            TituloSeccion("Actividad", Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaDato(etiqueta = "Este mes", valor = "$esteMes", detalle = "entrenos", modifier = Modifier.weight(1f))
                TarjetaDato(
                    etiqueta = "Racha",
                    valor = "$racha",
                    detalle = if (racha == 1) "semana" else "semanas",
                    modifier = Modifier.weight(1f)
                )
                TarjetaDato(etiqueta = "Total", valor = "${lista.size}", detalle = "entrenos", modifier = Modifier.weight(1f))
            }
        }

        item {
            Tarjeta {
                CalendarioEntrenos(
                    mes = mes,
                    diasEntrenados = diasDelMes,
                    onMesAnterior = onMesAnterior,
                    onMesSiguiente = onMesSiguiente
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonSecundario(
                    texto = "Estadísticas",
                    onClick = onEstadisticas,
                    icono = Icons.AutoMirrored.Rounded.ShowChart,
                    modifier = Modifier.weight(1f)
                )
                BotonSecundario(
                    texto = "Historial",
                    onClick = onHistorial,
                    icono = Icons.Rounded.History,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        lista.firstOrNull()?.let { ultimo ->
            item {
                TituloSeccion("Último entreno", Modifier.fillMaxWidth())
                TarjetaEntrenamiento(entrenamiento = ultimo, onClick = { onVerEntrenamiento(ultimo.id) })
            }
        }
    }
}
