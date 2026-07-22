package com.gympulse.android.ui.registro

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

// Pantalla de registro. Igual que el login, todavia no crea nada: cuando exista el backend, el boton llamara a la API.
@Composable
fun PantallaRegistro(
    onRegistroCorrecto: () -> Unit,
    onVolverALogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var usuario by remember { mutableStateOf("")}
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var repetirContrasena by remember { mutableStateOf("") }

    // Se recalcula solo en cada tecla, porque depende de estado. Cuando cambia, el boton se activa o se desactiva sin que hagamos nada.
    val datosValidos = email.isNotBlank() &&
            contrasena.isNotBlank() &&
            contrasena == repetirContrasena

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Empieza a registrar tus entrenamientos",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Nombre de usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contrasena") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // isError pinta el campo en rojo. Solo avisa si ya se ha escrito algo, para no marcar error nada mas entrar.
        OutlinedTextField(
            value = repetirContrasena,
            onValueChange = { repetirContrasena = it },
            label = { Text("Repetir contrasena") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = repetirContrasena.isNotBlank() && contrasena != repetirContrasena,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRegistroCorrecto,
            enabled = datosValidos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear cuenta")
        }

        // TextButton es un boton sin fondo, para acciones secundarias.
        TextButton(
            onClick = onVolverALogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ya tengo una cuenta")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaRegistroPreview() {
    GymPulseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PantallaRegistro(onRegistroCorrecto = {}, onVolverALogin = {})
        }
    }
}
