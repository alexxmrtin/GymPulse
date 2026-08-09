package com.gympulse.android.data.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/*
 El backend devuelve siempre el mismo JSON de error: {timestamp, codigo, mensaje}. Estas funciones
 lo sacan de la excepcion de Retrofit para poder enseñar el mensaje real ("El email ya esta
 registrado") en vez de un "HTTP 409" que no le dice nada al usuario.
*/
@Serializable
data class ErrorApi(
    val timestamp: String? = null,
    val codigo: Int? = null,
    val mensaje: String? = null
)

private val json = Json { ignoreUnknownKeys = true }

fun Throwable.mensajeParaUsuario(): String = when (this) {
    is HttpException -> mensajeDelCuerpo() ?: "Error del servidor (${code()})"
    is IOException -> "No se puede conectar con el servidor"
    else -> "Ha ocurrido un error inesperado"
}

fun Throwable.codigoHttp(): Int? = (this as? HttpException)?.code()

private fun HttpException.mensajeDelCuerpo(): String? = runCatching {
    val cuerpo = response()?.errorBody()?.string()
    if (cuerpo.isNullOrBlank()) null else json.decodeFromString<ErrorApi>(cuerpo).mensaje
}.getOrNull()
