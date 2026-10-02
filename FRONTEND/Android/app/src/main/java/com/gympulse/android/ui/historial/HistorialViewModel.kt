package com.gympulse.android.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gympulse.android.data.repositorios.EntrenamientoRepositorio
import com.gympulse.android.ui.comun.Cargador
import com.gympulse.android.ui.comun.fabricaViewModel

class HistorialViewModel(entrenamientos: EntrenamientoRepositorio) : ViewModel() {

    private val cargador = Cargador(viewModelScope) { entrenamientos.mios() }

    val estado = cargador.estado
    val refrescando = cargador.refrescando

    fun cargar(mostrarIndicador: Boolean = false) = cargador.cargar(mostrarIndicador)

    companion object {
        val Fabrica = fabricaViewModel { HistorialViewModel(it.entrenamientos) }
    }
}
