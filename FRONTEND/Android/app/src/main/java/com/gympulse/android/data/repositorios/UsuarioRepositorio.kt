package com.gympulse.android.data.repositorios

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.gympulse.android.data.api.ApiSesion
import com.gympulse.android.data.api.GymPulseApi
import com.gympulse.android.data.modelo.ActualizarUsuarioPeticion
import com.gympulse.android.data.modelo.LoginGooglePeticion
import com.gympulse.android.data.modelo.LoginPeticion
import com.gympulse.android.data.modelo.RefrescoPeticion
import com.gympulse.android.data.modelo.ReenviarVerificacionPeticion
import com.gympulse.android.data.modelo.RegistroPeticion
import com.gympulse.android.data.modelo.Sesion
import com.gympulse.android.data.modelo.Usuario
import com.gympulse.android.data.sesion.AlmacenSesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

class UsuarioRepositorio(
    private val api: GymPulseApi,
    private val apiSesion: ApiSesion,
    private val almacen: AlmacenSesion,
    private val gestorCredenciales: CredentialManager
) {

    private val ambito = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun registrar(
        username: String,
        email: String,
        password: String
    ): Result<String> = runCatching {
        api.registrar(RegistroPeticion(username, email, password)).mensaje
    }

    suspend fun reenviarVerificacion(email: String): Result<String> = runCatching {
        api.reenviarVerificacion(ReenviarVerificacionPeticion(email)).mensaje
    }

    suspend fun login(email: String, password: String): Result<Sesion> = runCatching {
        val sesion = api.login(LoginPeticion(email, password))
        almacen.guardar(sesion.token, sesion.tokenRefresco, sesion.usuario.id)
        sesion
    }

    suspend fun loginConGoogle(idTokenDeGoogle: String): Result<Sesion> = runCatching {
        val sesion = api.loginConGoogle(LoginGooglePeticion(idTokenDeGoogle))
        almacen.guardar(sesion.token, sesion.tokenRefresco, sesion.usuario.id)
        sesion
    }

    suspend fun miPerfil(): Result<Usuario> = runCatching {
        val id = almacen.usuarioId ?: error("No hay sesion iniciada")
        api.usuario(id)
    }

    suspend fun actualizarMiPerfil(
        peso: Double?,
        altura: Double?,
        fechaNacimiento: LocalDate?
    ): Result<Usuario> = runCatching {
        val id = almacen.usuarioId ?: error("No hay sesion iniciada")
        api.actualizarUsuario(id, ActualizarUsuarioPeticion(peso, altura, fechaNacimiento))
    }

    suspend fun eliminarMiCuenta(): Result<Unit> = runCatching {
        val id = almacen.usuarioId ?: error("No hay sesion iniciada")
        api.eliminarUsuario(id)
        almacen.invalidar()
    }

    fun cerrarSesion() {
        val tokenRefresco = almacen.tokenRefresco()
        almacen.invalidar()
        if (tokenRefresco != null) {
            ambito.launch { runCatching { apiSesion.cerrarSesion(RefrescoPeticion(tokenRefresco)) } }
        }
        ambito.launch { runCatching { gestorCredenciales.clearCredentialState(ClearCredentialStateRequest()) } }
    }
}
