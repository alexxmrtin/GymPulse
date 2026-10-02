package com.gympulse.android.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val LINEAS_GUIA = 3

@Composable
fun GraficaBarras(
    valores: List<Int>,
    etiquetas: List<String>,
    describirSeleccion: (indice: Int) -> String,
    modifier: Modifier = Modifier
) {
    if (valores.isEmpty()) return

    var seleccion by rememberSaveable(valores.size) { mutableIntStateOf(valores.lastIndex) }
    val primario = MaterialTheme.colorScheme.primary
    val guia = MaterialTheme.colorScheme.outlineVariant
    val maximo = (valores.maxOrNull() ?: 0).coerceAtLeast(1)

    Column(modifier = modifier) {
        Text(
            text = describirSeleccion(seleccion),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .semantics { contentDescription = describirSeleccion(seleccion) }
                .pointerInput(valores) {
                    detectTapGestures { posicion ->
                        seleccion = indiceEnX(posicion.x, size.width.toFloat(), valores.size)
                    }
                }
                .pointerInput(valores) {
                    detectHorizontalDragGestures { cambio, _ ->
                        seleccion = indiceEnX(cambio.position.x, size.width.toFloat(), valores.size)
                    }
                }
        ) {
            dibujarLineasGuia(guia)

            val hueco = size.width / valores.size
            val ancho = hueco * 0.56f
            val radio = 4.dp.toPx()

            valores.forEachIndexed { indice, valor ->
                val izquierda = hueco * indice + (hueco - ancho) / 2
                val alto = if (valor == 0) 2.dp.toPx() else (valor / maximo.toFloat()) * size.height
                val color = when {
                    valor == 0 -> guia
                    indice == seleccion -> primario
                    else -> primario.copy(alpha = 0.45f)
                }
                val barra = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = izquierda,
                            top = size.height - alto,
                            right = izquierda + ancho,
                            bottom = size.height,
                            topLeftCornerRadius = CornerRadius(radio),
                            topRightCornerRadius = CornerRadius(radio)
                        )
                    )
                }
                drawPath(barra, color)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            etiquetas.forEachIndexed { indice, etiqueta ->
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (indice == seleccion) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun GraficaLinea(
    valores: List<Double>,
    etiquetaInicio: String,
    etiquetaFin: String,
    formatearEje: (Double) -> String,
    describirSeleccion: (indice: Int) -> String,
    modifier: Modifier = Modifier
) {
    if (valores.size < 2) return

    var seleccion by rememberSaveable(valores.size) { mutableIntStateOf(valores.lastIndex) }
    val primario = MaterialTheme.colorScheme.primary
    val guia = MaterialTheme.colorScheme.outlineVariant
    val fondo = MaterialTheme.colorScheme.surfaceContainer
    val estiloEje = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
    val medidor = rememberTextMeasurer()

    val minimo = valores.min()
    val maximo = valores.max()
    val margen = ((maximo - minimo) * 0.15).takeIf { it > 0 } ?: 1.0
    val bajo = (minimo - margen).coerceAtLeast(0.0)
    val alto = maximo + margen

    Column(modifier = modifier) {
        Text(
            text = describirSeleccion(seleccion),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .semantics { contentDescription = describirSeleccion(seleccion) }
                .pointerInput(valores) {
                    detectTapGestures { posicion ->
                        seleccion = puntoMasCercano(posicion.x, size.width.toFloat(), valores.size)
                    }
                }
                .pointerInput(valores) {
                    detectHorizontalDragGestures { cambio, _ ->
                        seleccion = puntoMasCercano(cambio.position.x, size.width.toFloat(), valores.size)
                    }
                }
        ) {
            val margenX = 8.dp.toPx()
            val margenY = 8.dp.toPx()
            val anchoUtil = size.width - margenX * 2
            val altoUtil = size.height - margenY * 2

            fun x(indice: Int) = margenX + anchoUtil * indice / (valores.size - 1)
            fun y(valor: Double) = margenY + altoUtil * (1 - ((valor - bajo) / (alto - bajo)).toFloat())

            dibujarLineasGuia(guia)
            repeat(LINEAS_GUIA) { linea ->
                val fraccion = linea / (LINEAS_GUIA - 1f)
                val valorEje = alto - (alto - bajo) * fraccion
                val texto = medidor.measure(formatearEje(valorEje), estiloEje)
                drawText(
                    textLayoutResult = texto,
                    topLeft = Offset(0f, (size.height * fraccion - texto.size.height - 2.dp.toPx()).coerceAtLeast(0f))
                )
            }

            val trazo = Path().apply {
                valores.forEachIndexed { indice, valor ->
                    if (indice == 0) moveTo(x(indice), y(valor)) else lineTo(x(indice), y(valor))
                }
            }
            val area = Path().apply {
                addPath(trazo)
                lineTo(x(valores.lastIndex), size.height)
                lineTo(x(0), size.height)
                close()
            }
            drawPath(area, Brush.verticalGradient(listOf(primario.copy(alpha = 0.18f), Color.Transparent)))

            drawLine(
                color = guia,
                start = Offset(x(seleccion), 0f),
                end = Offset(x(seleccion), size.height),
                strokeWidth = 1.dp.toPx()
            )

            drawPath(
                path = trazo,
                color = primario,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            valores.forEachIndexed { indice, valor ->
                val centro = Offset(x(indice), y(valor))
                val radio = if (indice == seleccion) 6.dp.toPx() else 4.dp.toPx()
                drawCircle(color = fondo, radius = radio + 2.dp.toPx(), center = centro)
                drawCircle(color = primario, radius = radio, center = centro)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(
                text = etiquetaInicio,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = etiquetaFin,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun DrawScope.dibujarLineasGuia(color: Color) {
    repeat(LINEAS_GUIA) { linea ->
        val y = size.height * linea / (LINEAS_GUIA - 1f)
        drawLine(
            color = color.copy(alpha = if (linea == LINEAS_GUIA - 1) 1f else 0.5f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun indiceEnX(x: Float, ancho: Float, cantidad: Int): Int =
    (x / (ancho / cantidad)).toInt().coerceIn(0, cantidad - 1)

private fun puntoMasCercano(x: Float, ancho: Float, cantidad: Int): Int =
    Math.round(x / ancho * (cantidad - 1)).coerceIn(0, cantidad - 1)

