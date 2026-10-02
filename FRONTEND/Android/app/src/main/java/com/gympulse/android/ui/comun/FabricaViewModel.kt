package com.gympulse.android.ui.comun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gympulse.android.AppGymPulse
import com.gympulse.android.Contenedor

inline fun <reified VM : ViewModel> fabricaViewModel(
    crossinline crear: CreationExtras.(Contenedor) -> VM
): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val aplicacion = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AppGymPulse
        crear(aplicacion.contenedor)
    }
}
