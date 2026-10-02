package com.gympulse.android.ui.idiomaajustes

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
fun PantallaIdiomaAjustes(onVolver: () -> Unit) {
    EsqueletoPantalla(titulo = "Idioma", onVolver = onVolver) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BloqueAjustes(modifier = Modifier.selectableGroup()) {
                FilaOpcion(texto = "Español", seleccionada = true, onSeleccionar = {})
                DivisorAjustes()
                FilaOpcion(
                    texto = "English",
                    descripcion = "Próximamente",
                    seleccionada = false,
                    habilitada = false,
                    onSeleccionar = {}
                )
            }
        }
    }
}
