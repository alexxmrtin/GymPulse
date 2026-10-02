package com.gympulse.android.ui.perfil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gympulse.android.data.api.mensajeParaUsuario
import com.gympulse.android.data.modelo.Entrenamiento
import com.gympulse.android.data.modelo.Usuario
import com.gympulse.android.data.perfil.FotoPerfilRepositorio
import com.gympulse.android.data.repositorios.EntrenamientoRepositorio
import com.gympulse.android.data.repositorios.UsuarioRepositorio
import com.gympulse.android.ui.comun.Cargador
import com.gympulse.android.ui.comun.fabricaViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class DatosPerfil(
    val usuario: Usuario,
    val entrenamientos: List<Entrenamiento>
)

class PerfilViewModel(
    private val usuarios: UsuarioRepositorio,
    private val entrenamientos: EntrenamientoRepositorio,
    private val fotoPerfil: FotoPerfilRepositorio
) : ViewModel() {

    private val cargador = Cargador(viewModelScope) {
        runCatching {
            coroutineScope {
                val usuario = async { usuarios.miPerfil().getOrThrow() }
                val lista = async { entrenamientos.mios().getOrThrow() }
                DatosPerfil(usuario.await(), lista.await())
            }
        }
    }

    val estado = cargador.estado
    val refrescando = cargador.refrescando

    private val _mes = MutableStateFlow(YearMonth.now())
    val mes: StateFlow<YearMonth> = _mes.asStateFlow()

    private val _subiendoFoto = MutableStateFlow(false)
    val subiendoFoto: StateFlow<Boolean> = _subiendoFoto.asStateFlow()

    private val _errores = Channel<String>(Channel.BUFFERED)
    val errores: Flow<String> = _errores.receiveAsFlow()

    fun cargar(mostrarIndicador: Boolean = false) = cargador.cargar(mostrarIndicador)

    fun cambiarFoto(origen: Uri) {
        if (_subiendoFoto.value) return
        _subiendoFoto.value = true

        viewModelScope.launch {
            fotoPerfil.subir(origen)
                .onSuccess { actualizado -> cargador.modificar { it.copy(usuario = actualizado) } }
                .onFailure { _errores.send(it.mensajeParaUsuario()) }
            _subiendoFoto.value = false
        }
    }

    fun quitarFoto() {
        viewModelScope.launch {
            fotoPerfil.quitar()
                .onSuccess { cargador.modificar { it.copy(usuario = it.usuario.copy(fotoActualizadaEn = null)) } }
                .onFailure { _errores.send(it.mensajeParaUsuario()) }
        }
    }

    fun mesAnterior() = _mes.update { it.minusMonths(1) }

    fun mesSiguiente() = _mes.update { if (it < YearMonth.now()) it.plusMonths(1) else it }

    companion object {
        val Fabrica = fabricaViewModel { PerfilViewModel(it.usuarios, it.entrenamientos, it.fotoPerfil) }
    }
}
