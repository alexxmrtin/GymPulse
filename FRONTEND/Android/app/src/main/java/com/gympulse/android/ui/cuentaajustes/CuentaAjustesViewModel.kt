package com.gympulse.android.ui.cuentaajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gympulse.android.data.api.mensajeParaUsuario
import com.gympulse.android.data.repositorios.UsuarioRepositorio
import com.gympulse.android.ui.comun.Cargador
import com.gympulse.android.ui.comun.fabricaViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CuentaAjustesViewModel(private val usuarios: UsuarioRepositorio) : ViewModel() {

    private val cargador = Cargador(viewModelScope) { usuarios.miPerfil() }

    val estado = cargador.estado

    private val _eliminando = MutableStateFlow(false)
    val eliminando: StateFlow<Boolean> = _eliminando.asStateFlow()

    private val _errores = Channel<String>(Channel.BUFFERED)
    val errores: Flow<String> = _errores.receiveAsFlow()

    init {
        cargador.cargar()
    }

    fun cargar() = cargador.cargar()

    fun eliminarCuenta() {
        if (_eliminando.value) return
        _eliminando.value = true

        viewModelScope.launch {
            usuarios.eliminarMiCuenta().onFailure { _errores.send(it.mensajeParaUsuario()) }
            _eliminando.value = false
        }
    }

    companion object {
        val Fabrica = fabricaViewModel { CuentaAjustesViewModel(it.usuarios) }
    }
}
