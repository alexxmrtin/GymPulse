package com.gympulse.android.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gympulse.android.data.repositorios.UsuarioRepositorio
import com.gympulse.android.ui.comun.Cargador
import com.gympulse.android.ui.comun.fabricaViewModel

class AjustesViewModel(private val usuarios: UsuarioRepositorio) : ViewModel() {

    private val cargador = Cargador(viewModelScope) { usuarios.miPerfil() }

    val usuario = cargador.estado

    fun cargar() = cargador.cargar()

    fun cerrarSesion() = usuarios.cerrarSesion()

    companion object {
        val Fabrica = fabricaViewModel { AjustesViewModel(it.usuarios) }
    }
}
