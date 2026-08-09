package com.gympulse.android.data.sesion

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion")

class AlmacenSesion(private val context: Context) {

    private val claveToken = stringPreferencesKey("token")
    private val claveUsuarioId = longPreferencesKey("usuarioId")

    private val ambito = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var tokenEnMemoria: String? = null

    @Volatile
    var usuarioId: Long? = null
        private set

    private val _sesionActiva = MutableStateFlow(false)
    val sesionActiva: StateFlow<Boolean> = _sesionActiva.asStateFlow()

    suspend fun cargar() {
        val guardado = context.dataStore.data.first()
        tokenEnMemoria = guardado[claveToken]
        usuarioId = guardado[claveUsuarioId]
        _sesionActiva.value = tokenEnMemoria != null
    }

    suspend fun guardar(token: String, usuarioId: Long) {
        tokenEnMemoria = token
        this.usuarioId = usuarioId
        _sesionActiva.value = true
        context.dataStore.edit { preferencias ->
            preferencias[claveToken] = token
            preferencias[claveUsuarioId] = usuarioId
        }
    }

    fun token(): String? = tokenEnMemoria

    fun invalidar() {
        tokenEnMemoria = null
        usuarioId = null
        _sesionActiva.value = false
        ambito.launch {
            context.dataStore.edit { it.clear() }
        }
    }
}
