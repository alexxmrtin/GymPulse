package com.gympulse.android

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gympulse.android.data.preferencias.Preferencias
import com.gympulse.android.ui.navegacion.NavegacionGymPulse
import com.gympulse.android.ui.theme.GymPulseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        val contenedor = (application as AppGymPulse).contenedor

        setContent {
            GymPulseTheme {
                val estadoSesion by contenedor.almacenSesion.estado.collectAsStateWithLifecycle()
                val preferencias by contenedor.preferencias.preferencias
                    .collectAsStateWithLifecycle(initialValue = Preferencias())

                NavegacionGymPulse(
                    estadoSesion = estadoSesion,
                    unidadPeso = preferencias.unidadPeso
                )
            }
        }
    }
}
