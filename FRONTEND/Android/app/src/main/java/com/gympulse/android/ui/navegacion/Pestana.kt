package com.gympulse.android.ui.navegacion

import androidx.annotation.DrawableRes
import com.gympulse.android.R

enum class Pestana(
    val etiqueta: String,
    @param:DrawableRes val icono: Int,
    val grafo: Any
) {
    ENTRENAMIENTO("Entrenamiento", R.drawable.ic_mancuerna, Rutas.GrafoEntrenamiento),
    PERFIL("Perfil", R.drawable.ic_perfil, Rutas.GrafoPerfil),
    AJUSTES("Ajustes", R.drawable.ic_ajustes, Rutas.GrafoAjustes)
}
