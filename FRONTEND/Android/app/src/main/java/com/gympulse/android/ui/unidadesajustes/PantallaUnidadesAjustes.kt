package com.gympulse.android.ui.unidadesajustes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympulse.android.data.preferencias.UnidadPeso
import com.gympulse.android.ui.ajustes.PreferenciasViewModel
import com.gympulse.android.ui.componentes.BloqueAjustes
import com.gympulse.android.ui.componentes.DivisorAjustes
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.FilaOpcion

@Composable
fun PantallaUnidadesAjustes(
    onVolver: () -> Unit,
    viewModel: PreferenciasViewModel = viewModel(factory = PreferenciasViewModel.Fabrica)
) {
    val preferencias by viewModel.preferencias.collectAsStateWithLifecycle()

    EsqueletoPantalla(titulo = "Unidades", onVolver = onVolver) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(relleno),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BloqueAjustes(titulo = "Peso", modifier = Modifier.selectableGroup()) {
                FilaOpcion(
                    texto = "Kilogramos (kg)",
                    seleccionada = preferencias.unidadPeso == UnidadPeso.KG,
                    onSeleccionar = { viewModel.cambiarUnidad(UnidadPeso.KG) }
                )
                DivisorAjustes()
                FilaOpcion(
                    texto = "Libras (lb)",
                    seleccionada = preferencias.unidadPeso == UnidadPeso.LB,
                    onSeleccionar = { viewModel.cambiarUnidad(UnidadPeso.LB) }
                )
            }
        }
    }
}
