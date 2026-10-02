package com.gympulse.android.ui.entrenamientoactivo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.gympulse.android.data.api.mensajeParaUsuario
import com.gympulse.android.data.modelo.Ejercicio
import com.gympulse.android.data.modelo.Entrenamiento
import com.gympulse.android.data.repositorios.EjercicioRepositorio
import com.gympulse.android.data.repositorios.EntrenamientoRepositorio
import com.gympulse.android.data.repositorios.SerieRepositorio
import com.gympulse.android.ui.comun.Cargador
import com.gympulse.android.ui.comun.datosOrNull
import com.gympulse.android.ui.comun.fabricaViewModel
import com.gympulse.android.ui.navegacion.Rutas
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class DatosEntrenamientoActivo(
    val entrenamiento: Entrenamiento,
    val ejercicios: List<Ejercicio>
)

sealed interface EventoActivo {
    data class Error(val mensaje: String) : EventoActivo
    data class SerieAnadida(val ejercicioNombre: String) : EventoActivo
    data class EjercicioCreado(val ejercicio: Ejercicio) : EventoActivo
    data class Terminado(val id: Long) : EventoActivo
    data object Descartado : EventoActivo
}

class EntrenamientoActivoViewModel(
    private val id: Long,
    estadoGuardado: SavedStateHandle,
    private val entrenamientos: EntrenamientoRepositorio,
    private val series: SerieRepositorio,
    private val ejercicios: EjercicioRepositorio
) : ViewModel() {

    val inicioMillis: Long = estadoGuardado.get<Long>(CLAVE_INICIO)
        ?: System.currentTimeMillis().also { estadoGuardado[CLAVE_INICIO] = it }

    private val cargador = Cargador(viewModelScope) {
        runCatching {
            coroutineScope {
                val entrenamiento = async { entrenamientos.porId(id).getOrThrow() }
                val catalogo = async { ejercicios.todos().getOrThrow() }
                DatosEntrenamientoActivo(entrenamiento.await(), catalogo.await().sortedBy { it.nombre.lowercase() })
            }
        }
    }

    val estado = cargador.estado

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    private val _eventos = Channel<EventoActivo>(Channel.BUFFERED)
    val eventos: Flow<EventoActivo> = _eventos.receiveAsFlow()

    val tieneSeries: Boolean
        get() = estado.value.datosOrNull()?.entrenamiento?.series?.isNotEmpty() == true

    init {
        cargador.cargar()
    }

    fun cargar() = cargador.cargar()

    fun anadirSerie(ejercicioId: Long, repeticiones: Int, pesoKg: Double) {
        if (_guardando.value) return
        _guardando.value = true

        viewModelScope.launch {
            series.crear(id, ejercicioId, repeticiones, pesoKg)
                .onSuccess { serie ->
                    cargador.modificar { datos ->
                        datos.copy(entrenamiento = datos.entrenamiento.copy(series = datos.entrenamiento.series + serie))
                    }
                    _eventos.send(EventoActivo.SerieAnadida(serie.ejercicioNombre))
                }
                .onFailure { _eventos.send(EventoActivo.Error(it.mensajeParaUsuario())) }
            _guardando.value = false
        }
    }

    fun eliminarSerie(serieId: Long) {
        val anterior = estado.value.datosOrNull() ?: return
        cargador.modificar { datos ->
            datos.copy(entrenamiento = datos.entrenamiento.copy(series = datos.entrenamiento.series.filterNot { it.id == serieId }))
        }

        viewModelScope.launch {
            series.eliminar(serieId).onFailure { error ->
                cargador.modificar { anterior }
                _eventos.send(EventoActivo.Error(error.mensajeParaUsuario()))
            }
        }
    }

    fun crearEjercicio(nombre: String, grupoMuscular: String?) {
        viewModelScope.launch {
            ejercicios.crear(nombre.trim(), grupoMuscular = grupoMuscular)
                .onSuccess { nuevo ->
                    cargador.modificar { datos ->
                        datos.copy(ejercicios = (datos.ejercicios + nuevo).sortedBy { it.nombre.lowercase() })
                    }
                    _eventos.send(EventoActivo.EjercicioCreado(nuevo))
                }
                .onFailure { _eventos.send(EventoActivo.Error(it.mensajeParaUsuario())) }
        }
    }

    fun terminar() {
        viewModelScope.launch { _eventos.send(EventoActivo.Terminado(id)) }
    }

    fun descartar() {
        viewModelScope.launch {
            entrenamientos.eliminar(id)
                .onSuccess { _eventos.send(EventoActivo.Descartado) }
                .onFailure { _eventos.send(EventoActivo.Error(it.mensajeParaUsuario())) }
        }
    }

    companion object {
        private const val CLAVE_INICIO = "inicioMillis"

        val Fabrica = fabricaViewModel { contenedor ->
            val estadoGuardado = createSavedStateHandle()
            EntrenamientoActivoViewModel(
                id = estadoGuardado.toRoute<Rutas.EntrenamientoActivo>().id,
                estadoGuardado = estadoGuardado,
                entrenamientos = contenedor.entrenamientos,
                series = contenedor.series,
                ejercicios = contenedor.ejercicios
            )
        }
    }
}
