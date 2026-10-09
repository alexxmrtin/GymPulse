package com.gympulse.android.ui.registro

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gympulse.android.data.api.mensajeParaUsuario
import com.gympulse.android.data.repositorios.UsuarioRepositorio
import com.gympulse.android.ui.comun.fabricaViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val LONGITUD_MINIMA_CONTRASENA = 6
const val LONGITUD_MAXIMA_CONTRASENA = 72

data class EstadoRegistro(
    val usuario: String = "",
    val email: String = "",
    val contrasena: String = "",
    val repetirContrasena: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val correoEnviado: Boolean = false
) {
    val errorEmail: String?
        get() = if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) "Email no válido" else null

    val errorContrasena: String?
        get() = when {
            contrasena.isNotEmpty() && contrasena.length < LONGITUD_MINIMA_CONTRASENA ->
                "Mínimo $LONGITUD_MINIMA_CONTRASENA caracteres"
            contrasena.length > LONGITUD_MAXIMA_CONTRASENA -> "Máximo $LONGITUD_MAXIMA_CONTRASENA caracteres"
            else -> null
        }

    val errorRepetir: String?
        get() = if (repetirContrasena.isNotEmpty() && repetirContrasena != contrasena) "Las contraseñas no coinciden" else null

    val puedeEnviar: Boolean
        get() = usuario.isNotBlank() && email.isNotBlank() && contrasena.isNotEmpty() &&
            repetirContrasena.isNotEmpty() && errorEmail == null && errorContrasena == null &&
            errorRepetir == null && !cargando
}

class RegistroViewModel(private val usuarios: UsuarioRepositorio) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoRegistro())
    val estado: StateFlow<EstadoRegistro> = _estado.asStateFlow()

    fun onUsuarioCambiado(valor: String) = _estado.update { it.copy(usuario = valor, error = null) }

    fun onEmailCambiado(valor: String) = _estado.update { it.copy(email = valor, error = null) }

    fun onContrasenaCambiada(valor: String) = _estado.update { it.copy(contrasena = valor, error = null) }

    fun onRepetirCambiada(valor: String) = _estado.update { it.copy(repetirContrasena = valor, error = null) }

    fun crearCuenta() {
        val actual = _estado.value
        if (!actual.puedeEnviar) return

        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val email = actual.email.trim()
            usuarios.registrar(actual.usuario.trim(), email, actual.contrasena)
                .onSuccess { _estado.update { it.copy(cargando = false, correoEnviado = true) } }
                .onFailure { error ->
                    _estado.update { it.copy(cargando = false, error = error.mensajeParaUsuario()) }
                }
        }
    }

    companion object {
        val Fabrica = fabricaViewModel { RegistroViewModel(it.usuarios) }
    }
}
