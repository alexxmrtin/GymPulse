package com.gympulse.android.ui.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.gympulse.android.data.modelo.Entrenamiento
import com.gympulse.android.data.repositorios.EntrenamientoRepositorio
import com.gympulse.android.ui.comun.Cargador
import com.gympulse.android.ui.comun.fabricaViewModel
import com.gympulse.android.ui.navegacion.Rutas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DatosEstadisticas(
    val entrenamientos: List<Entrenamiento>,
    val resumen: Estadisticas
)

class EstadisticasViewModel(
    ejercicioInicial: Long?,
    entrenamientos: EntrenamientoRepositorio
) : ViewModel() {

    private val cargador = Cargador(viewModelScope) {
        entrenamientos.mios().map { lista -> DatosEstadisticas(lista, calcularEstadisticas(lista)) }
    }

    val estado = cargador.estado

    private val _ejercicioSeleccionado = MutableStateFlow(ejercicioInicial)
    val ejercicioSeleccionado: StateFlow<Long?> = _ejercicioSeleccionado.asStateFlow()

    init {
        cargador.cargar()
    }

    fun cargar() = cargador.cargar()

    fun seleccionarEjercicio(id: Long) {
        _ejercicioSeleccionado.value = id
    }

    companion object {
        val Fabrica = fabricaViewModel { contenedor ->
            EstadisticasViewModel(
                ejercicioInicial = createSavedStateHandle().toRoute<Rutas.Estadisticas>().ejercicioId,
                entrenamientos = contenedor.entrenamientos
            )
        }
    }
}
