package com.gympulse.android.ui.temaajustes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gympulse.android.ui.componentes.BloqueAjustes
import com.gympulse.android.ui.componentes.DivisorAjustes
import com.gympulse.android.ui.componentes.EsqueletoPantalla
import com.gympulse.android.ui.componentes.FilaOpcion

@Composable
fun PantallaTemaAjustes(onVolver: () -> Unit) {
    EsqueletoPantalla(titulo = "Tema", onVolver = onVolver) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BloqueAjustes(modifier = Modifier.selectableGroup()) {
                FilaOpcion(texto = "Oscuro", seleccionada = true, onSeleccionar = {})
                DivisorAjustes()
                FilaOpcion(
                    texto = "Claro",
                    descripcion = "Próximamente",
                    seleccionada = false,
                    habilitada = false,
                    onSeleccionar = {}
                )
            }
        }
    }
}
