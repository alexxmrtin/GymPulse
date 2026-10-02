package com.gympulse.android.ui.navegacion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.gympulse.android.data.preferencias.UnidadPeso
import com.gympulse.android.data.sesion.EstadoSesion
import com.gympulse.android.ui.ajustes.PantallaAjustes
import com.gympulse.android.ui.comun.LocalRellenoInferior
import com.gympulse.android.ui.comun.LocalUnidadPeso
import com.gympulse.android.ui.componentes.BarraInferior
import com.gympulse.android.ui.componentes.FondoMarca
import com.gympulse.android.ui.cuentaajustes.PantallaCuentaAjustes
import com.gympulse.android.ui.detalleentrenamiento.PantallaDetalleEntrenamiento
import com.gympulse.android.ui.ejercicios.PantallaEjercicios
import com.gympulse.android.ui.entrenamiento.PantallaEntrenamiento
import com.gympulse.android.ui.entrenamientoactivo.PantallaEntrenamientoActivo
import com.gympulse.android.ui.estadisticas.PantallaEstadisticas
import com.gympulse.android.ui.historial.PantallaHistorial
import com.gympulse.android.ui.idiomaajustes.PantallaIdiomaAjustes
import com.gympulse.android.ui.login.PantallaLogin
import com.gympulse.android.ui.notificacionesajustes.PantallaNotificacionesAjustes
import com.gympulse.android.ui.perfil.PantallaPerfil
import com.gympulse.android.ui.perfilajustes.PantallaPerfilAjustes
import com.gympulse.android.ui.registro.PantallaRegistro
import com.gympulse.android.ui.temaajustes.PantallaTemaAjustes
import com.gympulse.android.ui.unidadesajustes.PantallaUnidadesAjustes

private const val DURACION_TRANSICION = 220

@Composable
fun NavegacionGymPulse(
    estadoSesion: EstadoSesion,
    unidadPeso: UnidadPeso,
    navController: NavHostController = rememberNavController()
) {
    if (estadoSesion == EstadoSesion.CARGANDO) {
        FondoMarca {}
        return
    }

    val inicio: Any = remember {
        if (estadoSesion == EstadoSesion.ACTIVA) Rutas.GrafoPerfil else Rutas.GrafoAutenticacion
    }

    LaunchedEffect(estadoSesion) {
        val destino = navController.currentDestination ?: return@LaunchedEffect
        val enAutenticacion = destino.hierarchy.any { it.hasRoute<Rutas.GrafoAutenticacion>() }

        val siguiente: Any? = when {
            estadoSesion == EstadoSesion.ACTIVA && enAutenticacion -> Rutas.GrafoPerfil
            estadoSesion == EstadoSesion.INACTIVA && !enAutenticacion -> Rutas.GrafoAutenticacion
            else -> null
        }
        if (siguiente != null) {
            navController.navigate(siguiente) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    val entradaActual by navController.currentBackStackEntryAsState()
    val pestanaActual = entradaActual?.destination?.let { destino ->
        Pestana.entries.firstOrNull { pestana -> destino.hierarchy.any { it.hasRoute(pestana.grafo::class) } }
    }

    CompositionLocalProvider(LocalUnidadPeso provides unidadPeso) {
        FondoMarca {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                bottomBar = {
                    AnimatedVisibility(
                        visible = pestanaActual != null,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        BarraInferior(
                            pestanaActual = pestanaActual,
                            onPestanaSeleccionada = { pestana ->
                                navController.navigate(pestana.grafo) {
                                    popUpTo<Rutas.Perfil> { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            ) { relleno ->
                CompositionLocalProvider(LocalRellenoInferior provides relleno.calculateBottomPadding()) {
                    NavHost(
                        navController = navController,
                        startDestination = inicio,
                        enterTransition = { fadeIn(tween(DURACION_TRANSICION)) + slideInHorizontally(tween(DURACION_TRANSICION)) { it / 10 } },
                        exitTransition = { fadeOut(tween(DURACION_TRANSICION)) },
                        popEnterTransition = { fadeIn(tween(DURACION_TRANSICION)) },
                        popExitTransition = { fadeOut(tween(DURACION_TRANSICION)) + slideOutHorizontally(tween(DURACION_TRANSICION)) { it / 10 } }
                    ) {
                        grafoAutenticacion(navController)
                        grafoEntrenamiento(navController)
                        grafoPerfil(navController)
                        grafoAjustes(navController)
                        pantallasCompartidas(navController)
                    }
                }
            }
        }
    }
}

private fun NavGraphBuilder.grafoAutenticacion(navController: NavHostController) {
    navigation<Rutas.GrafoAutenticacion>(startDestination = Rutas.Login) {
        composable<Rutas.Login> {
            PantallaLogin(onIrARegistro = { navController.navigate(Rutas.Registro) })
        }
        composable<Rutas.Registro> { entrada ->
            PantallaRegistro(onVolver = { navController.volverDesde(entrada) })
        }
    }
}

private fun NavGraphBuilder.grafoEntrenamiento(navController: NavHostController) {
    navigation<Rutas.GrafoEntrenamiento>(startDestination = Rutas.Entrenamiento) {
        composable<Rutas.Entrenamiento> {
            PantallaEntrenamiento(
                onEntrenamientoEmpezado = { id -> navController.navigate(Rutas.EntrenamientoActivo(id)) },
                onContinuar = { id -> navController.navigate(Rutas.EntrenamientoActivo(id)) },
                onEjercicios = { navController.navigate(Rutas.Ejercicios) },
                onHistorial = { navController.navigate(Rutas.Historial) },
                onVerEntrenamiento = { id -> navController.navigate(Rutas.DetalleEntrenamiento(id)) }
            )
        }
        composable<Rutas.Ejercicios> { entrada ->
            PantallaEjercicios(
                onVolver = { navController.volverDesde(entrada) },
                onVerProgresion = { id -> navController.navigate(Rutas.Estadisticas(id)) }
            )
        }
    }
}

private fun NavGraphBuilder.grafoPerfil(navController: NavHostController) {
    navigation<Rutas.GrafoPerfil>(startDestination = Rutas.Perfil) {
        composable<Rutas.Perfil> {
            PantallaPerfil(
                onEditarPerfil = { navController.navigate(Rutas.PerfilAjustes) },
                onEstadisticas = { navController.navigate(Rutas.Estadisticas()) },
                onHistorial = { navController.navigate(Rutas.Historial) },
                onVerEntrenamiento = { id -> navController.navigate(Rutas.DetalleEntrenamiento(id)) }
            )
        }
    }
}

private fun NavGraphBuilder.grafoAjustes(navController: NavHostController) {
    navigation<Rutas.GrafoAjustes>(startDestination = Rutas.Ajustes) {
        composable<Rutas.Ajustes> {
            PantallaAjustes(
                onPerfil = { navController.navigate(Rutas.PerfilAjustes) },
                onCuenta = { navController.navigate(Rutas.CuentaAjustes) },
                onNotificaciones = { navController.navigate(Rutas.NotificacionesAjustes) },
                onIdioma = { navController.navigate(Rutas.IdiomaAjustes) },
                onTema = { navController.navigate(Rutas.TemaAjustes) },
                onUnidades = { navController.navigate(Rutas.UnidadesAjustes) }
            )
        }
        composable<Rutas.CuentaAjustes> { entrada ->
            PantallaCuentaAjustes(onVolver = { navController.volverDesde(entrada) })
        }
        composable<Rutas.NotificacionesAjustes> { entrada ->
            PantallaNotificacionesAjustes(onVolver = { navController.volverDesde(entrada) })
        }
        composable<Rutas.IdiomaAjustes> { entrada ->
            PantallaIdiomaAjustes(onVolver = { navController.volverDesde(entrada) })
        }
        composable<Rutas.TemaAjustes> { entrada ->
            PantallaTemaAjustes(onVolver = { navController.volverDesde(entrada) })
        }
        composable<Rutas.UnidadesAjustes> { entrada ->
            PantallaUnidadesAjustes(onVolver = { navController.volverDesde(entrada) })
        }
    }
}

private fun NavGraphBuilder.pantallasCompartidas(navController: NavHostController) {
    composable<Rutas.EntrenamientoActivo> { entrada ->
        PantallaEntrenamientoActivo(
            onVolver = { navController.volverDesde(entrada) },
            onTerminado = { id ->
                val vieneDelDetalle = navController.previousBackStackEntry?.destination?.hasRoute<Rutas.DetalleEntrenamiento>() == true
                if (vieneDelDetalle) {
                    navController.volverDesde(entrada)
                } else {
                    navController.navigate(Rutas.DetalleEntrenamiento(id)) {
                        popUpTo<Rutas.EntrenamientoActivo> { inclusive = true }
                    }
                }
            },
            onDescartado = {
                navController.volverDesde(entrada)
                if (navController.currentDestination?.hasRoute<Rutas.DetalleEntrenamiento>() == true) {
                    navController.popBackStack()
                }
            }
        )
    }
    composable<Rutas.DetalleEntrenamiento> { entrada ->
        PantallaDetalleEntrenamiento(
            onVolver = { navController.volverDesde(entrada) },
            onAnadirSeries = { id -> navController.navigate(Rutas.EntrenamientoActivo(id)) },
            onEliminado = { navController.volverDesde(entrada) }
        )
    }
    composable<Rutas.Historial> { entrada ->
        PantallaHistorial(
            onVolver = { navController.volverDesde(entrada) },
            onVerEntrenamiento = { id -> navController.navigate(Rutas.DetalleEntrenamiento(id)) }
        )
    }
    composable<Rutas.Estadisticas> { entrada ->
        PantallaEstadisticas(onVolver = { navController.volverDesde(entrada) })
    }
    composable<Rutas.PerfilAjustes> { entrada ->
        PantallaPerfilAjustes(onVolver = { navController.volverDesde(entrada) })
    }
}

private fun NavHostController.volverDesde(entrada: NavBackStackEntry) {
    if (entrada.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        popBackStack()
    }
}
