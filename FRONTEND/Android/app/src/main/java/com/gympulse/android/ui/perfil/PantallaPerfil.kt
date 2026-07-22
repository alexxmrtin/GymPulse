package com.gympulse.android.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gympulse.android.R


private val TAMANO_FOTO = 120.dp
private val GRIS_APAGADO = Color.White.copy(alpha = 0.6f)


@Composable
fun PantallaPerfil(
    modifier: Modifier = Modifier,
    onEditarFoto: () -> Unit = {},
    onEstadisticas: () -> Unit = {},
    onHistorial: () -> Unit = {}
) {
    Column(modifier = modifier.padding(24.dp)) {
        Text(
            text = "Alexxmrtin", //Esta hardcodeado, en el futuro llamar a la API
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.Top) {

            // La foto y su boton van apilados, por eso hace falta una Column dentro del Row. CenterHorizontally centra el boton respecto a la foto.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Box(
                    modifier = Modifier
                        .size(TAMANO_FOTO)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    // tint pinta el icono de ese color, ignorando el que tenga el archivo. Blanco al 40% para que se lea como un hueco vacio y no como contenido real.
                    Icon(
                        painter = painterResource(R.drawable.ic_perfil),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.size(72.dp),
                        tint = Color.White.copy(alpha = 0.4f)
                    )
                }

                TextButton(
                    onClick = onEditarFoto,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Editar",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            //hardcodeado, hacer fetch
            Column(
                modifier = Modifier.height(TAMANO_FOTO),
                verticalArrangement = Arrangement.Center
            ) {
                DatoUsuario(etiqueta = "Peso", valor = "78 kg")
                DatoUsuario(etiqueta = "Altura", valor = "1,80 m")
                DatoUsuario(etiqueta = "Edad", valor = "26 años")
            }
        }

        SeparadorDifuminado()

        // Los dias entrenados estan en duro; cuando exista el backend vendran de la API.
        val diasEntrenados = setOf(2, 3, 6, 9, 10, 13, 16, 17, 20, 23)

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "Entrenos:",
                style = MaterialTheme.typography.bodyMedium,
                color = GRIS_APAGADO
            )

            Spacer(Modifier.width(20.dp))

            // size cuenta cuantos dias hay en el conjunto, asi el numero no se puede desincronizar del calendario.
            Text(
                text = diasEntrenados.size.toString(),
                style = MaterialTheme.typography.bodyMedium ,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(20.dp))

        //Podemos llamar a la funcion directamente porque estan en el mismo paquete
        CalendarioEntrenos(diasEntrenados = diasEntrenados)

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onEstadisticas,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Estadisticas")
            }

            Button(
                onClick = onHistorial,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Historial")
            }
        }
    }
}

@Composable
private fun SeparadorDifuminado() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun DatoUsuario(etiqueta: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // El ancho fijo alinea todos los valores en la misma columna, aunque las etiquetas midan distinto.
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = GRIS_APAGADO,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
