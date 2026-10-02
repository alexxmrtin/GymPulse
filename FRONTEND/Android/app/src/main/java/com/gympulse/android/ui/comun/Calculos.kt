package com.gympulse.android.ui.comun

import com.gympulse.android.data.modelo.Entrenamiento
import com.gympulse.android.data.modelo.Serie
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class GrupoSeries(
    val ejercicioId: Long,
    val ejercicioNombre: String,
    val series: List<Serie>
) {
    val pesoMaximo: Double get() = series.maxOf { it.peso }
}

val Serie.volumen: Double get() = repeticiones * peso

val Entrenamiento.volumen: Double get() = series.sumOf { it.volumen }

val Entrenamiento.numeroEjercicios: Int get() = series.distinctBy { it.ejercicioId }.size

val Entrenamiento.titulo: String get() = nombre?.takeIf { it.isNotBlank() } ?: "Entrenamiento"

fun List<Serie>.agruparPorEjercicio(): List<GrupoSeries> =
    sortedBy { it.id }
        .groupBy { it.ejercicioId }
        .map { (id, series) -> GrupoSeries(id, series.first().ejercicioNombre, series) }

fun LocalDate.inicioDeSemana(): LocalDate = with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun List<Entrenamiento>.rachaSemanas(hoy: LocalDate = LocalDate.now()): Int {
    val semanas = map { it.fecha.inicioDeSemana() }.toSet()
    var semana = hoy.inicioDeSemana()
    if (semana !in semanas) semana = semana.minusWeeks(1)
    var racha = 0
    while (semana in semanas) {
        racha++
        semana = semana.minusWeeks(1)
    }
    return racha
}
