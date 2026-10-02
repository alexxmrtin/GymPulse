package com.gympulse.android.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gympulse.android.ui.comun.numeroEditable
import com.gympulse.android.ui.comun.parsearDecimal

@Composable
fun SelectorNumerico(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    paso: Double,
    modifier: Modifier = Modifier,
    sufijo: String? = null,
    decimales: Boolean = false
) {
    fun desplazar(delta: Double) {
        val actual = parsearDecimal(valor) ?: 0.0
        val nuevo = (actual + delta).coerceAtLeast(0.0)
        onCambio(if (decimales) numeroEditable(nuevo) else nuevo.toInt().toString())
    }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotonPaso(Icons.Rounded.Remove, "Restar $etiqueta") { desplazar(-paso) }
            BasicTextField(
                value = valor,
                onValueChange = { nuevo ->
                    val limpio = nuevo.filter { it.isDigit() || (decimales && (it == ',' || it == '.')) }
                    onCambio(limpio.take(6))
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (decimales) KeyboardType.Decimal else KeyboardType.Number
                ),
                modifier = Modifier.weight(1f)
            )
            BotonPaso(Icons.Rounded.Add, "Sumar $etiqueta") { desplazar(paso) }
        }
        if (sufijo != null) {
            Text(
                text = sufijo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BotonPaso(
    icono: ImageVector,
    descripcion: String,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.size(40.dp)
    ) {
        Icon(icono, contentDescription = descripcion)
    }
}
