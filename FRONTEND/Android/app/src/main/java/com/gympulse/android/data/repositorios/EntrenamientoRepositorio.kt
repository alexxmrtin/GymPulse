package com.gympulse.android.data.repositorios

import com.gympulse.android.data.api.GymPulseApi
import com.gympulse.android.data.modelo.CrearEntrenamientoPeticion
import com.gympulse.android.data.modelo.Entrenamiento
import java.time.LocalDate

class EntrenamientoRepositorio(private val api: GymPulseApi) {

    suspend fun mios(): Result<List<Entrenamiento>> = runCatching {
        api.misEntrenamientos()
    }

    suspend fun porId(id: Long): Result<Entrenamiento> = runCatching {
        api.entrenamiento(id)
    }

    suspend fun crear(
        nombre: String? = null,
        fecha: LocalDate = LocalDate.now()
    ): Result<Entrenamiento> = runCatching {
        api.crearEntrenamiento(CrearEntrenamientoPeticion(nombre, fecha))
    }

    suspend fun eliminar(id: Long): Result<Unit> = runCatching {
        api.eliminarEntrenamiento(id)
    }

    /*
     Los dias del mes en los que hay entrenamiento, para pintar el calendario del perfil.
    */
    suspend fun diasEntrenadosDelMes(anio: Int, mes: Int): Result<Set<Int>> = mios().map { lista ->
        lista.filter { it.fecha.year == anio && it.fecha.monthValue == mes }
            .map { it.fecha.dayOfMonth }
            .toSet()
    }
}
