package com.gympulse.android.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gympulse.android.data.preferencias.AlmacenPreferencias
import com.gympulse.android.data.preferencias.Preferencias
import com.gympulse.android.data.preferencias.UnidadPeso
import com.gympulse.android.ui.comun.fabricaViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferenciasViewModel(private val almacen: AlmacenPreferencias) : ViewModel() {

    val preferencias: StateFlow<Preferencias> = almacen.preferencias.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Preferencias()
    )

    fun cambiarUnidad(unidad: UnidadPeso) {
        viewModelScope.launch { almacen.cambiarUnidad(unidad) }
    }

    fun cambiarRecordatorio(activo: Boolean) {
        viewModelScope.launch { almacen.cambiarRecordatorio(activo) }
    }

    fun cambiarResumenSemanal(activo: Boolean) {
        viewModelScope.launch { almacen.cambiarResumenSemanal(activo) }
    }

    companion object {
        val Fabrica = fabricaViewModel { PreferenciasViewModel(it.preferencias) }
    }
}
