package com.gympulse.android.ui.estadisticas

import com.gympulse.android.data.modelo.Entrenamiento
import com.gympulse.android.ui.comun.inicioDeSemana
import com.gympulse.android.ui.comun.volumen
import java.time.LocalDate

const val SEMANAS_ACTIVIDAD = 8

data class EjercicioEntrenado(
    val id: Long,
    val nombre: String,
    val series: Int
)

data class PuntoProgresion(
    val fecha: LocalDate,
    val pesoMaximo: Double
)

data class Progresion(
    val ejercicio: EjercicioEntrenado,
    val puntos: List<PuntoProgresion>,
    val record: Double
)

data class Estadisticas(
    val totalEntrenos: Int,
    val totalSeries: Int,
    val volumenTotal: Double,
    val ultimos30Dias: Int,
    val semanas: List<Pair<LocalDate, Int>>,
    val ejercicios: List<EjercicioEntrenado>
)

fun calcularEstadisticas(lista: List<Entrenamiento>, hoy: LocalDate = LocalDate.now()): Estadisticas {
    val series = lista.flatMap { it.series }
    val semanaActual = hoy.inicioDeSemana()
    val porSemana = lista.groupingBy { it.fecha.inicioDeSemana() }.eachCount()

    return Estadisticas(
        totalEntrenos = lista.size,
        totalSeries = series.size,
        volumenTotal = lista.sumOf { it.volumen },
        ultimos30Dias = lista.count { !it.fecha.isBefore(hoy.minusDays(29)) && !it.fecha.isAfter(hoy) },
        semanas = (SEMANAS_ACTIVIDAD - 1 downTo 0).map { atras ->
            val inicio = semanaActual.minusWeeks(atras.toLong())
            inicio to (porSemana[inicio] ?: 0)
        },
        ejercicios = series.groupBy { it.ejercicioId }
            .map { (id, delEjercicio) -> EjercicioEntrenado(id, delEjercicio.first().ejercicioNombre, delEjercicio.size) }
            .sortedByDescending { it.series }
    )
}

fun calcularProgresion(lista: List<Entrenamiento>, ejercicio: EjercicioEntrenado): Progresion {
    val puntos = lista
        .mapNotNull { entrenamiento ->
            entrenamiento.series
                .filter { it.ejercicioId == ejercicio.id }
                .maxOfOrNull { it.peso }
                ?.let { entrenamiento.fecha to it }
        }
        .groupBy({ it.first }, { it.second })
        .map { (fecha, pesos) -> PuntoProgresion(fecha, pesos.max()) }
        .sortedBy { it.fecha }

    return Progresion(
        ejercicio = ejercicio,
        puntos = puntos,
        record = puntos.maxOfOrNull { it.pesoMaximo } ?: 0.0
    )
}
