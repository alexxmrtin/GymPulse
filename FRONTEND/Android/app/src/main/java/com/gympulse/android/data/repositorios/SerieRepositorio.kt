package com.gympulse.android.data.repositorios

import com.gympulse.android.data.api.GymPulseApi
import com.gympulse.android.data.modelo.CrearSeriePeticion
import com.gympulse.android.data.modelo.Serie

class SerieRepositorio(private val api: GymPulseApi) {

    suspend fun deEntrenamiento(entrenamientoId: Long): Result<List<Serie>> = runCatching {
        api.seriesDeEntrenamiento(entrenamientoId)
    }

    /*
     Todas mis series de un ejercicio concreto, que es lo que necesita la pantalla de estadisticas
     para pintar la progresion de peso.
    */
    suspend fun deEjercicio(ejercicioId: Long): Result<List<Serie>> = runCatching {
        api.seriesDeEjercicio(ejercicioId)
    }

    suspend fun crear(
        entrenamientoId: Long,
        ejercicioId: Long,
        repeticiones: Int,
        peso: Double
    ): Result<Serie> = runCatching {
        api.crearSerie(CrearSeriePeticion(entrenamientoId, ejercicioId, repeticiones, peso))
    }

    suspend fun eliminar(id: Long): Result<Unit> = runCatching {
        api.eliminarSerie(id)
    }
}
