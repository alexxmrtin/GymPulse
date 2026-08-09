package com.gympulse.android.data.modelo

import kotlinx.serialization.Serializable

@Serializable
data class Ejercicio(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null,
    val grupoMuscular: String? = null
)

@Serializable
data class CrearEjercicioPeticion(
    val nombre: String,
    val descripcion: String? = null,
    val grupoMuscular: String? = null
)
