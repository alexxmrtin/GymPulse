package com.gympulse.android.ui.registro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympulse.android.ui.componentes.AvisoError
import com.gympulse.android.ui.componentes.BotonPrincipal
import com.gympulse.android.ui.componentes.CampoContrasena
import com.gympulse.android.ui.componentes.CampoTexto
import com.gympulse.android.ui.theme.GymPulseTheme

@Composable
fun PantallaRegistro(
    onVolver: () -> Unit,
    viewModel: RegistroViewModel = viewModel(factory = RegistroViewModel.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    if (estado.correoEnviado) {
        CorreoEnviado(email = estado.email.trim(), onIrALogin = onVolver)
        return
    }

    ContenidoRegistro(
        estado = estado,
        onUsuarioCambiado = viewModel::onUsuarioCambiado,
        onEmailCambiado = viewModel::onEmailCambiado,
        onContrasenaCambiada = viewModel::onContrasenaCambiada,
        onRepetirCambiada = viewModel::onRepetirCambiada,
        onCrearCuenta = viewModel::crearCuenta,
        onVolver = onVolver
    )
}

@Composable
private fun ContenidoRegistro(
    estado: EstadoRegistro,
    onUsuarioCambiado: (String) -> Unit,
    onEmailCambiado: (String) -> Unit,
    onContrasenaCambiada: (String) -> Unit,
    onRepetirCambiada: (String) -> Unit,
    onCrearCuenta: () -> Unit,
    onVolver: () -> Unit
) {
    val foco = LocalFocusManager.current

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onVolver, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }

            Spacer(Modifier.height(16.dp))
            Text(text = "Crear cuenta", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Empieza a registrar tus entrenamientos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            CampoTexto(
                valor = estado.usuario,
                onCambio = { onUsuarioCambiado(it.take(30)) },
                etiqueta = "Nombre de usuario",
                icono = Icons.Rounded.Person
            )
            Spacer(Modifier.height(12.dp))
            CampoTexto(
                valor = estado.email,
                onCambio = onEmailCambiado,
                etiqueta = "Email",
                icono = Icons.Rounded.AlternateEmail,
                teclado = KeyboardType.Email,
                error = estado.errorEmail
            )
            Spacer(Modifier.height(12.dp))
            CampoContrasena(
                valor = estado.contrasena,
                onCambio = onContrasenaCambiada,
                etiqueta = "Contraseña",
                error = estado.errorContrasena
            )
            Spacer(Modifier.height(12.dp))
            CampoContrasena(
                valor = estado.repetirContrasena,
                onCambio = onRepetirCambiada,
                etiqueta = "Repetir contraseña",
                error = estado.errorRepetir,
                accionIme = ImeAction.Done,
                alAccionIme = {
                    foco.clearFocus()
                    onCrearCuenta()
                }
            )

            AnimatedVisibility(visible = estado.error != null) {
                AvisoError(
                    mensaje = estado.error.orEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            BotonPrincipal(
                texto = "Crear cuenta",
                onClick = {
                    foco.clearFocus()
                    onCrearCuenta()
                },
                habilitado = estado.puedeEnviar,
                cargando = estado.cargando
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onVolver) {
                    Text("Inicia sesión")
                }
            }
        }
    }
}

@Composable
private fun CorreoEnviado(email: String, onIrALogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MarkEmailRead,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(28.dp))
        Text(text = "Revisa tu correo", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Te hemos enviado un enlace a $email para activar la cuenta. Ábrelo y después inicia sesión.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Si no lo ves, mira en la carpeta de spam. Desde el login puedes pedir que te lo reenviemos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(32.dp))
        BotonPrincipal(texto = "Ir a iniciar sesión", onClick = onIrALogin)
    }
}

@Preview
@Composable
private fun ContenidoRegistroPreview() {
    GymPulseTheme {
        ContenidoRegistro(
            estado = EstadoRegistro(usuario = "alex", email = "alex@", contrasena = "123"),
            onUsuarioCambiado = {},
            onEmailCambiado = {},
            onContrasenaCambiada = {},
            onRepetirCambiada = {},
            onCrearCuenta = {},
            onVolver = {}
        )
    }
}
