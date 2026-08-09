package com.gympulse.android.data.repositorios

import com.gympulse.android.data.api.GymPulseApi
import com.gympulse.android.data.modelo.ActualizarUsuarioPeticion
import com.gympulse.android.data.modelo.LoginPeticion
import com.gympulse.android.data.modelo.RegistroPeticion
import com.gympulse.android.data.modelo.Sesion
import com.gympulse.android.data.modelo.Usuario
import com.gympulse.android.data.sesion.AlmacenSesion
import java.time.LocalDate

class UsuarioRepositorio(
    private val api: GymPulseApi,
    private val almacen: AlmacenSesion
) {

    suspend fun registrar(
        username: String,
        email: String,
        password: String
    ): Result<Usuario> = runCatching {
        api.registrar(RegistroPeticion(username, email, password))
    }

    suspend fun login(email: String, password: String): Result<Sesion> = runCatching {
        val sesion = api.login(LoginPeticion(email, password))
        almacen.guardar(sesion.token, sesion.usuario.id)
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

    fun cerrarSesion() {
        almacen.invalidar()
    }
}
