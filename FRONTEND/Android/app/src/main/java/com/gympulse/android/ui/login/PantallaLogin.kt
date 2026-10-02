package com.gympulse.android.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
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
import com.gympulse.android.ui.componentes.LogoMarca
import com.gympulse.android.ui.theme.GymPulseTheme

@Composable
fun PantallaLogin(
    onIrARegistro: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    ContenidoLogin(
        estado = estado,
        onEmailCambiado = viewModel::onEmailCambiado,
        onContrasenaCambiada = viewModel::onContrasenaCambiada,
        onEntrar = viewModel::entrar,
        onIrARegistro = onIrARegistro
    )
}

@Composable
private fun ContenidoLogin(
    estado: EstadoLogin,
    onEmailCambiado: (String) -> Unit,
    onContrasenaCambiada: (String) -> Unit,
    onEntrar: () -> Unit,
    onIrARegistro: () -> Unit
) {
    val foco = LocalFocusManager.current

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            LogoMarca()
            Spacer(Modifier.height(28.dp))
            Text(text = "Bienvenido a GymPulse", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Inicia sesión para seguir registrando tus entrenos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(36.dp))

            CampoTexto(
                valor = estado.email,
                onCambio = onEmailCambiado,
                etiqueta = "Email",
                icono = Icons.Rounded.AlternateEmail,
                teclado = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))
            CampoContrasena(
                valor = estado.contrasena,
                onCambio = onContrasenaCambiada,
                etiqueta = "Contraseña",
                accionIme = ImeAction.Done,
                alAccionIme = {
                    foco.clearFocus()
                    onEntrar()
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
                texto = "Entrar",
                onClick = {
                    foco.clearFocus()
                    onEntrar()
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
                    text = "¿No tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onIrARegistro) {
                    Text("Crear cuenta")
                }
            }
        }
    }
}

@Preview
@Composable
private fun ContenidoLoginPreview() {
    GymPulseTheme {
        ContenidoLogin(
            estado = EstadoLogin(email = "alex@gympulse.com", error = "Email o contraseña incorrectos"),
            onEmailCambiado = {},
            onContrasenaCambiada = {},
            onEntrar = {},
            onIrARegistro = {}
        )
    }
}
