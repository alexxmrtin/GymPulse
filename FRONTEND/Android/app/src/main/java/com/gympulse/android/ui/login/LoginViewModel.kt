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
    val error: String? = null,
    val pendienteVerificacion: Boolean = false,
    val aviso: String? = null,
    val reenviando: Boolean = false,
    val cargandoGoogle: Boolean = false
) {
    val puedeEnviar: Boolean get() = email.isNotBlank() && contrasena.isNotBlank() && !cargando && !cargandoGoogle
}

class LoginViewModel(private val usuarios: UsuarioRepositorio) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoLogin())
    val estado: StateFlow<EstadoLogin> = _estado.asStateFlow()

    fun onEmailCambiado(email: String) =
        _estado.update { it.copy(email = email, error = null, pendienteVerificacion = false, aviso = null) }

    fun onContrasenaCambiada(contrasena: String) =
        _estado.update { it.copy(contrasena = contrasena, error = null, pendienteVerificacion = false, aviso = null) }

    fun entrar() {
        val actual = _estado.value
        if (!actual.puedeEnviar) return

        _estado.update { it.copy(cargando = true, error = null, pendienteVerificacion = false, aviso = null) }
        viewModelScope.launch {
            usuarios.login(actual.email.trim(), actual.contrasena).onFailure { error ->
                val codigo = error.codigoHttp()
                val mensaje = if (codigo == 401) "Email o contraseña incorrectos" else error.mensajeParaUsuario()
                _estado.update { it.copy(cargando = false, error = mensaje, pendienteVerificacion = codigo == 403) }
            }
        }
    }

    fun empezarGoogle() = _estado.update {
        it.copy(cargandoGoogle = true, error = null, pendienteVerificacion = false, aviso = null)
    }

    fun terminarGoogle(resultado: ResultadoGoogle) {
        when (resultado) {
            is ResultadoGoogle.Token -> entrarConGoogle(resultado.idToken)
            ResultadoGoogle.Cancelado -> _estado.update { it.copy(cargandoGoogle = false) }
            ResultadoGoogle.SinCuentas -> _estado.update {
                it.copy(cargandoGoogle = false, error = "No hay ninguna cuenta de Google en este dispositivo. Añade una en los ajustes del móvil.")
            }
            is ResultadoGoogle.Error -> _estado.update { it.copy(cargandoGoogle = false, error = resultado.mensaje) }
        }
    }

    private fun entrarConGoogle(idToken: String) {
        viewModelScope.launch {
            usuarios.loginConGoogle(idToken).onFailure { error ->
                _estado.update { it.copy(cargandoGoogle = false, error = error.mensajeParaUsuario()) }
            }
        }
    }

    fun reenviarVerificacion() {
        val actual = _estado.value
        if (actual.reenviando || actual.email.isBlank()) return

        _estado.update { it.copy(reenviando = true) }
        viewModelScope.launch {
            usuarios.reenviarVerificacion(actual.email.trim())
                .onSuccess {
                    _estado.update {
                        it.copy(
                            reenviando = false,
                            error = null,
                            pendienteVerificacion = false,
                            aviso = "Te hemos reenviado el correo. Abre el enlace y vuelve a entrar."
                        )
                    }
                }
                .onFailure { error -> _estado.update { it.copy(reenviando = false, error = error.mensajeParaUsuario()) } }
        }
    }

    companion object {
        val Fabrica = fabricaViewModel { LoginViewModel(it.usuarios) }
    }
}
