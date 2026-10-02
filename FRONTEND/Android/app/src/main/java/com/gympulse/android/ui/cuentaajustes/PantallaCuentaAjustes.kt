package com.gympulse.android.ui.cuentaajustes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympulse.android.ui.componentes.BloqueAjustes
import com.gympulse.android.ui.componentes.ContenidoSegunEstado
import com.gympulse.android.ui.componentes.DialogoConfirmacion
import com.gympulse.android.ui.componentes.DivisorAjustes
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.FilaAjuste

@Composable
fun PantallaCuentaAjustes(
    onVolver: () -> Unit,
    viewModel: CuentaAjustesViewModel = viewModel(factory = CuentaAjustesViewModel.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val eliminando by viewModel.eliminando.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmar by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.errores.collect { snackbar.showSnackbar(it) }
    }

    if (confirmar) {
        DialogoConfirmacion(
            titulo = "¿Eliminar tu cuenta?",
            texto = "Se borrarán tu cuenta y todos tus entrenamientos de forma permanente. No se puede deshacer.",
            textoConfirmar = "Eliminar cuenta",
            destructivo = true,
            onConfirmar = {
                confirmar = false
                viewModel.eliminarCuenta()
            },
            onCancelar = { confirmar = false }
        )
    }

    EsqueletoPantalla(titulo = "Cuenta", onVolver = onVolver, snackbar = snackbar) { relleno ->
        ContenidoSegunEstado(estado = estado, onReintentar = viewModel::cargar) { usuario ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(relleno),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                BloqueAjustes(titulo = "Datos de acceso") {
                    FilaAjuste(
                        texto = "Usuario",
                        valor = usuario.username,
                        icono = Icons.Rounded.Person,
                        onClick = null
                    )
                    DivisorAjustes()
                    FilaAjuste(
                        texto = "Email",
                        valor = usuario.email,
                        icono = Icons.Rounded.AlternateEmail,
                        onClick = null
                    )
                }

                Text(
                    text = "El usuario y el email no se pueden cambiar por ahora.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                BloqueAjustes(titulo = "Zona peligrosa") {
                    FilaAjuste(
                        texto = if (eliminando) "Eliminando…" else "Eliminar cuenta",
                        onClick = { if (!eliminando) confirmar = true },
                        icono = Icons.Rounded.DeleteForever,
                        destructivo = true
                    )
                }
            }
        }
    }
}
