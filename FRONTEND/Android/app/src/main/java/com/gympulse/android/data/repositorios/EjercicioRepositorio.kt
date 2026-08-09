package com.gympulse.android.data.repositorios

import com.gympulse.android.data.api.GymPulseApi
import com.gympulse.android.data.modelo.CrearEjercicioPeticion
import com.gympulse.android.data.modelo.Ejercicio

class EjercicioRepositorio(private val api: GymPulseApi) {

    suspend fun todos(): Result<List<Ejercicio>> = runCatching {
        api.ejercicios()
    }

    suspend fun buscarPorNombre(nombre: String): Result<List<Ejercicio>> = runCatching {
        api.ejercicios(nombre = nombre)
    }

    suspend fun porGrupoMuscular(grupoMuscular: String): Result<List<Ejercicio>> = runCatching {
        api.ejercicios(grupoMuscular = grupoMuscular)
    }

    suspend fun crear(
        nombre: String,
        descripcion: String? = null,
        grupoMuscular: String? = null
    ): Result<Ejercicio> = runCatching {
        api.crearEjercicio(CrearEjercicioPeticion(nombre, descripcion, grupoMuscular))
    }

    suspend fun eliminar(id: Long): Result<Unit> = runCatching {
        api.eliminarEjercicio(id)
    }
}
