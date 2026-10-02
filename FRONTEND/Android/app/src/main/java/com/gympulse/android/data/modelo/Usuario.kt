package com.gympulse.android.data.modelo

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Usuario(
    val id: Long,
    val username: String,
    val email: String,
    val peso: Double? = null,
    val altura: Double? = null,
    @Serializable(with = SerializadorLocalDate::class)
    val fechaNacimiento: LocalDate? = null,
    val fotoActualizadaEn: String? = null
)

@Serializable
data class Sesion(
    val token: String,
    val tipo: String,
    val expiraEnSegundos: Long,
    val usuario: Usuario
)

@Serializable
data class RegistroPeticion(
    val username: String,
    val email: String,
    val password: String,
    val peso: Double? = null,
    val altura: Double? = null,
    @Serializable(with = SerializadorLocalDate::class)
    val fechaNacimiento: LocalDate? = null
)

@Serializable
data class LoginPeticion(
    val email: String,
    val password: String
)

@Serializable
data class ActualizarUsuarioPeticion(
    val peso: Double? = null,
    val altura: Double? = null,
    @Serializable(with = SerializadorLocalDate::class)
    val fechaNacimiento: LocalDate? = null
)
