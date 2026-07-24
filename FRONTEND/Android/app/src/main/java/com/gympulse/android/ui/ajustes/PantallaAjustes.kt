package com.gympulse.android.ui.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gympulse.android.ui.componentes.SeparadorDifuminado

private val GRIS_APAGADO = Color.White.copy(alpha = 0.6f)

@Composable
fun PantallaAjustes(
    modifier: Modifier = Modifier,
    onPerfil: () -> Unit = {},
    onCuenta: () -> Unit = {},
    onNotificaciones: () -> Unit = {},
    onIdioma: () -> Unit = {},
    onTema: () -> Unit = {},
    onUnidades: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    Column(modifier = modifier.padding(24.dp)) {
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineLarge
        )

        SeparadorDifuminado()

        BloqueAjustes(titulo = "Cuenta") {
            FilaAjuste(texto = "Perfil", onClick = onPerfil)
            FilaAjuste(texto = "Cuenta", onClick = onCuenta)
            FilaAjuste(texto = "Notificaciones", onClick = onNotificaciones)
        }

        Spacer(Modifier.height(24.dp))

        BloqueAjustes(titulo = "Accesibilidad") {
            FilaAjuste(texto = "Idioma", onClick = onIdioma)
            FilaAjuste(texto = "Tema", onClick = onTema)
            FilaAjuste(texto = "Notificaciones", onClick = onNotificaciones)
            FilaAjuste(texto = "Unidades", onClick = onUnidades)
        }

        Spacer(Modifier.height(24.dp))

        BloqueAjustes {
            FilaAjuste(
                texto = "Cerrar sesion",
                onClick = onCerrarSesion,
                color = Color(0xFFE53935)
            )
        }
    }
}

@Composable
private fun BloqueAjustes(
    titulo: String? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    if (titulo != null) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = GRIS_APAGADO,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f)),
        content = contenido
    )
}

@Composable
private fun FilaAjuste(
    texto: String,
    onClick: () -> Unit,
    color: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
    }
}
