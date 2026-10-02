package com.gympulse.android.ui.ajustes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympulse.android.BuildConfig
import com.gympulse.android.data.perfil.urlFoto
import com.gympulse.android.ui.comun.datosOrNull
import com.gympulse.android.ui.componentes.Avatar
import com.gympulse.android.ui.componentes.BloqueAjustes
import com.gympulse.android.ui.componentes.DialogoConfirmacion
import com.gympulse.android.ui.componentes.DivisorAjustes
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.FilaAjuste
import com.gympulse.android.ui.componentes.Tarjeta

@Composable
fun PantallaAjustes(
    onPerfil: () -> Unit,
    onCuenta: () -> Unit,
    onNotificaciones: () -> Unit,
    onIdioma: () -> Unit,
    onTema: () -> Unit,
    onUnidades: () -> Unit,
    viewModel: AjustesViewModel = viewModel(factory = AjustesViewModel.Fabrica),
    preferenciasViewModel: PreferenciasViewModel = viewModel(factory = PreferenciasViewModel.Fabrica)
) {
    val usuario by viewModel.usuario.collectAsStateWithLifecycle()
    val preferencias by preferenciasViewModel.preferencias.collectAsStateWithLifecycle()
    var confirmarCierre by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.cargar() }

    if (confirmarCierre) {
        DialogoConfirmacion(
            titulo = "¿Cerrar sesión?",
            texto = "Tendrás que volver a iniciar sesión para ver tus entrenamientos.",
            textoConfirmar = "Cerrar sesión",
            destructivo = true,
            onConfirmar = {
                confirmarCierre = false
                viewModel.cerrarSesion()
            },
            onCancelar = { confirmarCierre = false }
        )
    }

    EsqueletoPantalla(titulo = "Ajustes") { relleno ->
        LazyColumn(
            contentPadding = relleno,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                val datos = usuario.datosOrNull()
                Tarjeta(onClick = onPerfil) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(nombre = datos?.username.orEmpty(), urlFoto = datos?.urlFoto(), tamano = 52.dp)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = datos?.username ?: "Tu perfil",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = datos?.email ?: "Peso, altura y fecha de nacimiento",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            item {
                BloqueAjustes(titulo = "Cuenta") {
                    FilaAjuste(texto = "Datos personales", onClick = onPerfil, icono = Icons.Rounded.Person)
                    DivisorAjustes()
                    FilaAjuste(texto = "Cuenta", onClick = onCuenta, icono = Icons.Rounded.ManageAccounts)
                    DivisorAjustes()
                    FilaAjuste(texto = "Notificaciones", onClick = onNotificaciones, icono = Icons.Rounded.Notifications)
                }
            }

            item {
                BloqueAjustes(titulo = "Preferencias") {
                    FilaAjuste(
                        texto = "Unidades",
                        onClick = onUnidades,
                        icono = Icons.Rounded.Straighten,
                        valor = preferencias.unidadPeso.simbolo
                    )
                    DivisorAjustes()
                    FilaAjuste(texto = "Idioma", onClick = onIdioma, icono = Icons.Rounded.Language, valor = "Español")
                    DivisorAjustes()
                    FilaAjuste(texto = "Tema", onClick = onTema, icono = Icons.Rounded.DarkMode, valor = "Oscuro")
                }
            }

            item {
                BloqueAjustes {
                    FilaAjuste(
                        texto = "Cerrar sesión",
                        onClick = { confirmarCierre = true },
                        icono = Icons.AutoMirrored.Rounded.Logout,
                        destructivo = true
                    )
                }
            }

            item {
                Text(
                    text = "GymPulse ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
