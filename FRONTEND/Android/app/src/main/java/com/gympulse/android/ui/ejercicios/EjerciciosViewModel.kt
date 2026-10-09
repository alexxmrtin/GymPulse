package com.gympulse.android.ui.ejercicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gympulse.android.data.api.mensajeParaUsuario
import com.gympulse.android.data.repositorios.EjercicioRepositorio
import com.gympulse.android.ui.comun.Cargador
import com.gympulse.android.ui.comun.fabricaViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val GRUPOS_MUSCULARES = listOf("Pecho", "Espalda", "Hombro", "Bíceps", "Tríceps", "Pierna", "Glúteo", "Core", "Cardio")

data class FiltroEjercicios(
    val busqueda: String = "",
    val grupo: String? = null,
    val soloPersonalizados: Boolean = false
)

sealed interface EventoEjercicios {
    data class Mensaje(val texto: String) : EventoEjercicios
    data object Creado : EventoEjercicios
    data object Eliminado : EventoEjercicios
}

class EjerciciosViewModel(private val ejercicios: EjercicioRepositorio) : ViewModel() {

    private val cargador = Cargador(viewModelScope) {
        ejercicios.todos().map { lista -> lista.sortedBy { it.nombre.lowercase() } }
    }

    val estado = cargador.estado

    private val _filtro = MutableStateFlow(FiltroEjercicios())
    val filtro: StateFlow<FiltroEjercicios> = _filtro.asStateFlow()

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    private val _eventos = Channel<EventoEjercicios>(Channel.BUFFERED)
    val eventos: Flow<EventoEjercicios> = _eventos.receiveAsFlow()

    init {
        cargador.cargar()
    }

    fun cargar() = cargador.cargar()

    fun onBusqueda(texto: String) = _filtro.update { it.copy(busqueda = texto) }

    fun onGrupo(grupo: String) = _filtro.update { it.copy(grupo = if (it.grupo == grupo) null else grupo) }

    fun onSoloPersonalizados() = _filtro.update { it.copy(soloPersonalizados = !it.soloPersonalizados) }

    fun crear(nombre: String, grupoMuscular: String?, descripcion: String) {
        if (_guardando.value || nombre.isBlank()) return
        _guardando.value = true

        viewModelScope.launch {
            ejercicios.crear(nombre.trim(), descripcion.trim().ifBlank { null }, grupoMuscular)
                .onSuccess { nuevo ->
                    cargador.modificar { lista -> (lista + nuevo).sortedBy { it.nombre.lowercase() } }
                    _eventos.send(EventoEjercicios.Creado)
                    _eventos.send(EventoEjercicios.Mensaje("«${nuevo.nombre}» añadido al catálogo"))
                }
                .onFailure { _eventos.send(EventoEjercicios.Mensaje(it.mensajeParaUsuario())) }
            _guardando.value = false
        }
    }

    fun eliminar(id: Long) {
        viewModelScope.launch {
            ejercicios.eliminar(id)
                .onSuccess {
                    cargador.modificar { lista -> lista.filterNot { it.id == id } }
                    _eventos.send(EventoEjercicios.Eliminado)
                }
                .onFailure { error ->
                    _eventos.send(EventoEjercicios.Mensaje(error.mensajeParaUsuario()))
                }
        }
    }

    companion object {
        val Fabrica = fabricaViewModel { EjerciciosViewModel(it.ejercicios) }
    }
}
