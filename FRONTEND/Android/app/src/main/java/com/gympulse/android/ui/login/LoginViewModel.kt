package com.gympulse.android.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gympulse.android.data.api.codigoHttp
import com.gympulse.android.data.api.mensajeParaUsuario
import com.gympulse.android.data.repositorios.UsuarioRepositorio
import com.gympulse.android.ui.comun.fabricaViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoLogin(
    val email: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val error: String? = null
) {
    val puedeEnviar: Boolean get() = email.isNotBlank() && contrasena.isNotBlank() && !cargando
}

class LoginViewModel(private val usuarios: UsuarioRepositorio) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoLogin())
    val estado: StateFlow<EstadoLogin> = _estado.asStateFlow()

    fun onEmailCambiado(email: String) = _estado.update { it.copy(email = email, error = null) }

    fun onContrasenaCambiada(contrasena: String) = _estado.update { it.copy(contrasena = contrasena, error = null) }

    fun entrar() {
        val actual = _estado.value
        if (!actual.puedeEnviar) return

        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            usuarios.login(actual.email.trim(), actual.contrasena).onFailure { error ->
                val mensaje = if (error.codigoHttp() == 401) "Email o contraseña incorrectos" else error.mensajeParaUsuario()
                _estado.update { it.copy(cargando = false, error = mensaje) }
            }
        }
    }

    companion object {
        val Fabrica = fabricaViewModel { LoginViewModel(it.usuarios) }
    }
}
