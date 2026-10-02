package com.gympulse.android.ui.detalleentrenamiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.gympulse.android.data.api.mensajeParaUsuario
import com.gympulse.android.data.repositorios.EntrenamientoRepositorio
import com.gympulse.android.ui.comun.Cargador
import com.gympulse.android.ui.comun.fabricaViewModel
import com.gympulse.android.ui.navegacion.Rutas
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface EventoDetalle {
    data object Eliminado : EventoDetalle
    data class Error(val mensaje: String) : EventoDetalle
}

class DetalleEntrenamientoViewModel(
    private val id: Long,
    private val entrenamientos: EntrenamientoRepositorio
) : ViewModel() {

    private val cargador = Cargador(viewModelScope) { entrenamientos.porId(id) }

    val estado = cargador.estado

    private val _eventos = Channel<EventoDetalle>(Channel.BUFFERED)
    val eventos: Flow<EventoDetalle> = _eventos.receiveAsFlow()

    fun cargar() = cargador.cargar()

    fun eliminar() {
        viewModelScope.launch {
            entrenamientos.eliminar(id)
                .onSuccess { _eventos.send(EventoDetalle.Eliminado) }
                .onFailure { _eventos.send(EventoDetalle.Error(it.mensajeParaUsuario())) }
        }
    }

    companion object {
        val Fabrica = fabricaViewModel { contenedor ->
            DetalleEntrenamientoViewModel(
                id = createSavedStateHandle().toRoute<Rutas.DetalleEntrenamiento>().id,
                entrenamientos = contenedor.entrenamientos
            )
        }
    }
}
