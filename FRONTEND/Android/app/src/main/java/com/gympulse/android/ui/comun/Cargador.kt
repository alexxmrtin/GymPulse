package com.gympulse.android.ui.comun

import com.gympulse.android.data.api.mensajeParaUsuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class Cargador<T>(
    private val ambito: CoroutineScope,
    private val fuente: suspend () -> Result<T>
) {
    private val _estado = MutableStateFlow<EstadoCarga<T>>(EstadoCarga.Cargando)
    val estado: StateFlow<EstadoCarga<T>> = _estado.asStateFlow()

    private val _refrescando = MutableStateFlow(false)
    val refrescando: StateFlow<Boolean> = _refrescando.asStateFlow()

    private var trabajo: Job? = null

    fun cargar(mostrarIndicador: Boolean = false) {
        if (trabajo?.isActive == true) return

        trabajo = ambito.launch {
            val habiaDatos = _estado.value is EstadoCarga.Listo
            if (!habiaDatos) {
                _estado.value = EstadoCarga.Cargando
            } else if (mostrarIndicador) {
                _refrescando.value = true
            }

            fuente()
                .onSuccess { _estado.value = EstadoCarga.Listo(it) }
                .onFailure { error ->
                    if (!habiaDatos) _estado.value = EstadoCarga.Error(error.mensajeParaUsuario())
                }

            _refrescando.value = false
        }
    }

    fun modificar(transformar: (T) -> T) {
        _estado.update { actual ->
            if (actual is EstadoCarga.Listo) EstadoCarga.Listo(transformar(actual.datos)) else actual
        }
    }
}
