package com.gympulse.android.ui.notificacionesajustes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympulse.android.ui.ajustes.PreferenciasViewModel
import com.gympulse.android.ui.componentes.BloqueAjustes
import com.gympulse.android.ui.componentes.DivisorAjustes
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.FilaInterruptor

@Composable
fun PantallaNotificacionesAjustes(
    onVolver: () -> Unit,
    viewModel: PreferenciasViewModel = viewModel(factory = PreferenciasViewModel.Fabrica)
) {
    val preferencias by viewModel.preferencias.collectAsStateWithLifecycle()

    EsqueletoPantalla(titulo = "Notificaciones", onVolver = onVolver) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(relleno),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BloqueAjustes {
                FilaInterruptor(
                    texto = "Recordatorio de entreno",
                    descripcion = "Un aviso los días que no hayas entrenado",
                    icono = Icons.Rounded.Alarm,
                    activo = preferencias.recordatorioEntreno,
                    onCambio = viewModel::cambiarRecordatorio
                )
                DivisorAjustes()
                FilaInterruptor(
                    texto = "Resumen semanal",
                    descripcion = "Tus entrenos y volumen de la semana, cada lunes",
                    icono = Icons.Rounded.Insights,
                    activo = preferencias.resumenSemanal,
                    onCambio = viewModel::cambiarResumenSemanal
                )
            }
            Text(
                text = "Tu elección se guarda en el dispositivo. El envío de avisos llegará en una próxima versión.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
