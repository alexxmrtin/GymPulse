package com.gympulse.android.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympulse.android.R
import com.gympulse.android.ui.componentes.AvisoError
import com.gympulse.android.ui.componentes.AvisoInformativo
import com.gympulse.android.ui.componentes.BotonPrincipal
import com.gympulse.android.ui.componentes.CampoContrasena
import com.gympulse.android.ui.componentes.CampoTexto
import com.gympulse.android.ui.componentes.LogoMarca
import com.gympulse.android.ui.theme.GymPulseTheme
import kotlinx.coroutines.launch

@Composable
fun PantallaLogin(
    onIrARegistro: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val actividad = LocalContext.current
    val ambito = rememberCoroutineScope()

    ContenidoLogin(
        estado = estado,
        onEmailCambiado = viewModel::onEmailCambiado,
        onContrasenaCambiada = viewModel::onContrasenaCambiada,
        onEntrar = viewModel::entrar,
        onReenviarVerificacion = viewModel::reenviarVerificacion,
        onGoogle = {
            viewModel.empezarGoogle()
            ambito.launch { viewModel.terminarGoogle(pedirTokenDeGoogle(actividad)) }
        },
        onIrARegistro = onIrARegistro
    )
}

@Composable
private fun ContenidoLogin(
    estado: EstadoLogin,
    onEmailCambiado: (String) -> Unit,
    onContrasenaCambiada: (String) -> Unit,
    onEntrar: () -> Unit,
    onReenviarVerificacion: () -> Unit,
    onGoogle: () -> Unit,
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

            AnimatedVisibility(visible = estado.pendienteVerificacion) {
                TextButton(
                    onClick = onReenviarVerificacion,
                    enabled = !estado.reenviando,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(if (estado.reenviando) "Enviando…" else "Reenviar correo de verificación")
                }
            }

            AnimatedVisibility(visible = estado.aviso != null) {
                AvisoInformativo(
                    mensaje = estado.aviso.orEmpty(),
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

            SeparadorO()

            BotonGoogle(
                onClick = {
                    foco.clearFocus()
                    onGoogle()
                },
                cargando = estado.cargandoGoogle,
                habilitado = !estado.cargando
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

@Composable
private fun SeparadorO() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = "o",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun BotonGoogle(onClick: () -> Unit, cargando: Boolean, habilitado: Boolean) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        } else {
            Image(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text("Continuar con Google", style = MaterialTheme.typography.labelLarge)
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
            onReenviarVerificacion = {},
            onGoogle = {},
            onIrARegistro = {}
        )
    }
}
