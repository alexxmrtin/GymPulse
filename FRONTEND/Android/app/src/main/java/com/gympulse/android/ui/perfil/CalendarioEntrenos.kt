package com.gympulse.android.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gympulse.android.ui.comun.nombreLargo
import java.time.LocalDate
import java.time.YearMonth

private val DIAS_SEMANA = listOf("L", "M", "X", "J", "V", "S", "D")

@Composable
fun CalendarioEntrenos(
    mes: YearMonth,
    diasEntrenados: Set<Int>,
    onMesAnterior: () -> Unit,
    onMesSiguiente: () -> Unit,
    modifier: Modifier = Modifier,
    hoy: LocalDate = LocalDate.now()
) {
    val huecosIniciales = mes.atDay(1).dayOfWeek.value - 1
    val casillas: List<Int?> = List(huecosIniciales) { null } + (1..mes.lengthOfMonth()).toList()
    val diaDeHoy = if (YearMonth.from(hoy) == mes) hoy.dayOfMonth else null

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = mes.nombreLargo(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onMesAnterior) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Mes anterior")
            }
            IconButton(onClick = onMesSiguiente, enabled = mes < YearMonth.from(hoy)) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Mes siguiente")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            DIAS_SEMANA.forEach { dia ->
                Text(
                    text = dia,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        casillas.chunked(7).forEach { semana ->
            Row(modifier = Modifier.fillMaxWidth()) {
                semana.forEach { dia ->
                    CasillaDia(
                        dia = dia,
                        entrenado = dia != null && dia in diasEntrenados,
                        esHoy = dia != null && dia == diaDeHoy,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(7 - semana.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CasillaDia(
    dia: Int?,
    entrenado: Boolean,
    esHoy: Boolean,
    modifier: Modifier = Modifier
) {
    val primario = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(if (entrenado) primario else Color.Transparent)
            .then(if (esHoy && !entrenado) Modifier.border(1.dp, primario, CircleShape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (dia != null) {
            Text(
                text = dia.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (entrenado || esHoy) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    entrenado -> MaterialTheme.colorScheme.onPrimary
                    esHoy -> primario
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                }
            )
        }
    }
}
