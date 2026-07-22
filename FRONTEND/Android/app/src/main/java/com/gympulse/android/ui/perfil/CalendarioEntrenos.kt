package com.gympulse.android.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.util.Locale

private val DIAS_SEMANA = listOf("L", "M", "X", "J", "V", "S", "D")

// Calendario del mes actual. Los dias entrenados se marcan con un circulo.
// De momento recibe los dias en duro desde la pantalla; cuando exista el backend, vendran de la API sin que cambie nada de aqui.
@Composable
fun CalendarioEntrenos(
    diasEntrenados: Set<Int>,
    modifier: Modifier = Modifier
) {
    // remember evita recalcular esto en cada redibujado: el mes no cambia mientras la pantalla este abierta.
    val mes = remember { YearMonth.now() }
    val totalDias = mes.lengthOfMonth()

    // dayOfWeek.value da 1 para lunes y 7 para domingo. Restando 1 sale cuantas casillas vacias hay que dejar antes del dia 1.
    val huecosIniciales = mes.atDay(1).dayOfWeek.value - 1

    // Las casillas vacias son null y los dias son numeros, por eso la lista es de Int?. chunked(7) la parte en grupos de siete: una semana por fila.
    val casillas: List<Int?> = List(huecosIniciales) { null } + (1..totalDias).toList()
    val semanas = casillas.chunked(7)

    Column(modifier = modifier) {

        // getDisplayName traduce el mes al idioma que se le pase; sin el saldria "JANUARY".
        Text(
            text = mes.month
                .getDisplayName(java.time.format.TextStyle.FULL, Locale.forLanguageTag("es"))
                .replaceFirstChar { it.uppercase() } + " " + mes.year,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            DIAS_SEMANA.forEach { dia ->
                // weight(1f) en los siete reparte el ancho a partes iguales, sea cual sea el tamano de la pantalla.
                Text(
                    text = dia,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        semanas.forEach { semana ->
            Row(modifier = Modifier.fillMaxWidth()) {
                semana.forEach { dia ->
                    CasillaDia(
                        dia = dia,
                        entrenado = dia != null && dia in diasEntrenados,
                        modifier = Modifier.weight(1f)
                    )
                }
                // La ultima semana puede tener menos de siete dias. Estos huecos mantienen el ancho de las casillas para que no se estiren.
                repeat(7 - semana.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// Una casilla del calendario. Si dia es null es un hueco antes del dia 1 y no se pinta nada.
@Composable
private fun CasillaDia(
    dia: Int?,
    entrenado: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        // aspectRatio(1f) fuerza que la casilla sea cuadrada: la altura la calcula a partir del ancho que le toco por el weight.
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(
                if (entrenado) MaterialTheme.colorScheme.primary
                else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        if (dia != null) {
            Text(
                text = dia.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (entrenado) Color.Black else Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
