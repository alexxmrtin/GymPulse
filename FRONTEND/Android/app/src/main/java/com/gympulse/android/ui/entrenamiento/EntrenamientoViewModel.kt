package com.gympulse.android.ui.entrenamiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gympulse.android.data.api.mensajeParaUsuario
import com.gympulse.android.data.modelo.Entrenamiento
import com.gympulse.android.data.repositorios.EntrenamientoRepositorio
import com.gympulse.android.ui.comun.Cargador
import com.gympulse.android.ui.comun.fabricaViewModel
import com.gympulse.android.ui.comun.inicioDeSemana
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ResumenEntrenamiento(
    val recientes: List<Entrenamiento>,
    val deHoy: Entrenamiento?,
    val estaSemana: Int
)

sealed interface EventoEntrenamiento {
    data class Empezado(val id: Long) : EventoEntrenamiento
    data class Error(val mensaje: String) : EventoEntrenamiento
}

class EntrenamientoViewModel(private val entrenamientos: EntrenamientoRepositorio) : ViewModel() {

    private val cargador = Cargador(viewModelScope) {
        entrenamientos.mios().map { lista ->
            val hoy = LocalDate.now()
            ResumenEntrenamiento(
                recientes = lista.take(3),
                deHoy = lista.firstOrNull { it.fecha == hoy },
                estaSemana = lista.count { it.fecha >= hoy.inicioDeSemana() }
            )
        }
    }

    val estado = cargador.estado
    val refrescando = cargador.refrescando

    private val _empezando = MutableStateFlow(false)
    val empezando: StateFlow<Boolean> = _empezando.asStateFlow()

    private val _eventos = Channel<EventoEntrenamiento>(Channel.BUFFERED)
    val eventos: Flow<EventoEntrenamiento> = _eventos.receiveAsFlow()

    fun cargar(mostrarIndicador: Boolean = false) = cargador.cargar(mostrarIndicador)

    fun empezar(nombre: String) {
        if (_empezando.value) return
        _empezando.value = true

        viewModelScope.launch {
            entrenamientos.crear(nombre = nombre.trim().ifBlank { null })
                .onSuccess { _eventos.send(EventoEntrenamiento.Empezado(it.id)) }
                .onFailure { _eventos.send(EventoEntrenamiento.Error(it.mensajeParaUsuario())) }
            _empezando.value = false
        }
    }

    companion object {
        val Fabrica = fabricaViewModel { EntrenamientoViewModel(it.entrenamientos) }
    }
}
