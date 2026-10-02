package com.gympulse.android.ui.navegacion

import kotlinx.serialization.Serializable

object Rutas {

    @Serializable data object GrafoAutenticacion
    @Serializable data object Login
    @Serializable data object Registro

    @Serializable data object GrafoEntrenamiento
    @Serializable data object Entrenamiento
    @Serializable data object Ejercicios
    @Serializable data class EntrenamientoActivo(val id: Long)

    @Serializable data object GrafoPerfil
    @Serializable data object Perfil
    @Serializable data object Historial
    @Serializable data class DetalleEntrenamiento(val id: Long)
    @Serializable data class Estadisticas(val ejercicioId: Long? = null)

    @Serializable data object GrafoAjustes
    @Serializable data object Ajustes
    @Serializable data object PerfilAjustes
    @Serializable data object CuentaAjustes
    @Serializable data object NotificacionesAjustes
    @Serializable data object IdiomaAjustes
    @Serializable data object TemaAjustes
    @Serializable data object UnidadesAjustes
}
