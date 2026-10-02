package com.gympulse.android.ui.componentes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gympulse.android.ui.comun.LocalRellenoInferior

val MARGEN_PANTALLA = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EsqueletoPantalla(
    titulo: String,
    modifier: Modifier = Modifier,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {},
    botonFlotante: @Composable () -> Unit = {},
    snackbar: SnackbarHostState? = null,
    contenido: @Composable (PaddingValues) -> Unit
) {
    val rellenoInferior = LocalRellenoInferior.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (onVolver != null) {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = acciones,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            Box(Modifier.padding(bottom = rellenoInferior)) { botonFlotante() }
        },
        snackbarHost = {
            if (snackbar != null) {
                SnackbarHost(snackbar, Modifier.padding(bottom = rellenoInferior))
            }
        }
    ) { interior ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = interior.calculateTopPadding())
                .clipToBounds()
        ) {
            contenido(
                PaddingValues(
                    start = MARGEN_PANTALLA,
                    end = MARGEN_PANTALLA,
                    top = 8.dp,
                    bottom = rellenoInferior + 24.dp
                )
            )
        }
    }
}

@Composable
fun PaddingValues.conExtraInferior(extra: Dp): PaddingValues {
    val direccion = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(direccion),
        end = calculateEndPadding(direccion),
        top = calculateTopPadding(),
        bottom = calculateBottomPadding() + extra
    )
}
