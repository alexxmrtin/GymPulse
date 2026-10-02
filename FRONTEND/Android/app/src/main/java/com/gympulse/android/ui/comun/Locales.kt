package com.gympulse.android.ui.comun

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gympulse.android.data.preferencias.UnidadPeso

val LocalRellenoInferior = staticCompositionLocalOf<Dp> { 0.dp }

val LocalUnidadPeso = staticCompositionLocalOf { UnidadPeso.KG }
