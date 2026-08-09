package com.gympulse.android.data.modelo

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Entrenamiento(
    val id: Long,
    val usuarioId: Long,
    val nombre: String? = null,
    @Serializable(with = SerializadorLocalDate::class)
    val fecha: LocalDate,
    val series: List<Serie> = emptyList()
)

@Serializable
data class CrearEntrenamientoPeticion(
    val nombre: String? = null,
    @Serializable(with = SerializadorLocalDate::class)
    val fecha: LocalDate
)
