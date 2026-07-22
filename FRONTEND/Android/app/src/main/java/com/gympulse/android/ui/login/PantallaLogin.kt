package com.gympulse.android.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gympulse.android.ui.theme.GymPulseTheme

// Pantalla de login. Todavia no valida nada: cuando exista el backend, el boton llamara a la API.
// KOTLIN: onLoginCorrecto es un parametro de tipo funcion, "() -> Unit" significa que no recibe nada y no devuelve nada. La pantalla no decide que pasa al entrar, solo avisa hacia arriba de que se ha pulsado; asi este componente sirve en cualquier contexto.
@Composable
fun PantallaLogin(
    onLoginCorrecto: () -> Unit,
    onIrARegistro: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ESTADO: esta es la pieza clave de Compose. mutableStateOf crea un valor que Compose vigila, y remember hace que sobreviva a los redibujados (sin el, se reiniciaria a "" cada vez que la pantalla se vuelve a pintar).
    // Cuando este valor cambia, Compose vuelve a ejecutar esta funcion sola y la pantalla se actualiza. No hay que refrescar nada a mano.
    // KOTLIN: el "by" es delegacion de propiedad; permite escribir email en vez de email.value. Por eso hacen falta los imports de getValue y setValue.
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        // Centra los hijos en vertical dentro de la columna.
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido a GymPulse!",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Inicia sesion para continuar",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // value es lo que se muestra y onValueChange lo que hacer al teclear. El campo no guarda nada por su cuenta: lee del estado y avisa de los cambios, y nosotros actualizamos el estado. Es el patron de input controlado de React.
        // KOTLIN: dentro de un bloque de un solo parametro, "it" es ese parametro sin necesidad de nombrarlo. Aqui it es el texto nuevo.
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // visualTransformation cambia como SE VE el texto, no lo que vale: sustituye cada caracter por un punto.
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contrasena") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // enabled se recalcula solo cada vez que cambian los campos, porque son estado. El boton se activa cuando los dos tienen algo escrito.
        Button(
            onClick = onLoginCorrecto,
            enabled = email.isNotBlank() && contrasena.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        // TextButton es un boton sin fondo, para acciones secundarias. Dentro de las llaves va CONTENIDO (un composable), por eso Text("...") y no text = "...".
        TextButton(
            onClick = onIrARegistro,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("No tengo una cuenta creada")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaLoginPreview() {
    GymPulseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PantallaLogin(onLoginCorrecto = {}, onIrARegistro = {})
        }
    }
}
