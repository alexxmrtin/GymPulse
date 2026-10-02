package com.gympulse.android.ui.perfilajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gympulse.android.data.api.mensajeParaUsuario
import com.gympulse.android.data.preferencias.AlmacenPreferencias
import com.gympulse.android.data.preferencias.UnidadPeso
import com.gympulse.android.data.repositorios.UsuarioRepositorio
import com.gympulse.android.ui.comun.fabricaViewModel
import com.gympulse.android.ui.comun.numeroEditable
import com.gympulse.android.ui.comun.parsearDecimal
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FormularioPerfil(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val unidad: UnidadPeso = UnidadPeso.KG,
    val peso: String = "",
    val altura: String = "",
    val fechaNacimiento: LocalDate? = null,
    val guardando: Boolean = false
) {
    val errorPeso: String?
        get() = if (peso.isNotBlank() && (parsearDecimal(peso) ?: -1.0) <= 0) "Peso no válido" else null

    val errorAltura: String?
        get() = if (altura.isNotBlank() && (parsearDecimal(altura) ?: -1.0) !in 50.0..260.0) "Entre 50 y 260 cm" else null

    val puedeGuardar: Boolean
        get() = !cargando && !guardando && errorPeso == null && errorAltura == null
}

sealed interface EventoPerfilAjustes {
    data object Guardado : EventoPerfilAjustes
    data class Error(val mensaje: String) : EventoPerfilAjustes
}

class PerfilAjustesViewModel(
    private val usuarios: UsuarioRepositorio,
    private val preferencias: AlmacenPreferencias
) : ViewModel() {

    private val _formulario = MutableStateFlow(FormularioPerfil())
    val formulario: StateFlow<FormularioPerfil> = _formulario.asStateFlow()

    private val _eventos = Channel<EventoPerfilAjustes>(Channel.BUFFERED)
    val eventos: Flow<EventoPerfilAjustes> = _eventos.receiveAsFlow()

    init {
        cargar()
    }

    fun cargar() {
        _formulario.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            val unidad = preferencias.preferencias.first().unidadPeso
            usuarios.miPerfil()
                .onSuccess { usuario ->
                    _formulario.value = FormularioPerfil(
                        cargando = false,
                        unidad = unidad,
                        peso = usuario.peso?.let { numeroEditable(unidad.desdeKg(it), 1) }.orEmpty(),
                        altura = usuario.altura?.let { numeroEditable(it, 0) }.orEmpty(),
                        fechaNacimiento = usuario.fechaNacimiento
                    )
                }
                .onFailure { error ->
                    _formulario.update { it.copy(cargando = false, errorCarga = error.mensajeParaUsuario()) }
                }
        }
    }

    fun onPeso(valor: String) = _formulario.update { it.copy(peso = valor) }

    fun onAltura(valor: String) = _formulario.update { it.copy(altura = valor) }

    fun onFechaNacimiento(fecha: LocalDate?) = _formulario.update { it.copy(fechaNacimiento = fecha) }

    fun guardar() {
        val actual = _formulario.value
        if (!actual.puedeGuardar) return

        _formulario.update { it.copy(guardando = true) }
        viewModelScope.launch {
            usuarios.actualizarMiPerfil(
                peso = parsearDecimal(actual.peso)?.let { actual.unidad.aKg(it) },
                altura = parsearDecimal(actual.altura),
                fechaNacimiento = actual.fechaNacimiento
            )
                .onSuccess { _eventos.send(EventoPerfilAjustes.Guardado) }
                .onFailure { _eventos.send(EventoPerfilAjustes.Error(it.mensajeParaUsuario())) }
            _formulario.update { it.copy(guardando = false) }
        }
    }

    companion object {
        val Fabrica = fabricaViewModel { PerfilAjustesViewModel(it.usuarios, it.preferencias) }
    }
}
