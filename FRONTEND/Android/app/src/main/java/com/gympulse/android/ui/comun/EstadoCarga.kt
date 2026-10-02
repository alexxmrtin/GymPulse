package com.gympulse.android.ui.comun

sealed interface EstadoCarga<out T> {
    data object Cargando : EstadoCarga<Nothing>
    data class Error(val mensaje: String) : EstadoCarga<Nothing>
    data class Listo<T>(val datos: T) : EstadoCarga<T>
}

fun <T> EstadoCarga<T>.datosOrNull(): T? = (this as? EstadoCarga.Listo)?.datos
