package com.gympulse.android.data.modelo

import kotlinx.serialization.Serializable

@Serializable
data class Serie(
    val id: Long,
    val entrenamientoId: Long,
    val ejercicioId: Long,
    val ejercicioNombre: String,
    val repeticiones: Int,
    val peso: Double
)

@Serializable
data class CrearSeriePeticion(
    val entrenamientoId: Long,
    val ejercicioId: Long,
    val repeticiones: Int,
    val peso: Double
)
