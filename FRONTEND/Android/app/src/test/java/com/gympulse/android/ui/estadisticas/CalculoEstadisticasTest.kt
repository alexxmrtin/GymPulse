package com.gympulse.android.ui.estadisticas

import com.gympulse.android.data.modelo.Entrenamiento
import com.gympulse.android.data.modelo.Serie
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CalculoEstadisticasTest {

    private val hoy = LocalDate.of(2026, 10, 2)

    private fun serie(id: Long, ejercicioId: Long, nombre: String, peso: Double) =
        Serie(id, entrenamientoId = 0, ejercicioId = ejercicioId, ejercicioNombre = nombre, repeticiones = 5, peso = peso)

    private val lista = listOf(
        Entrenamiento(1, 1, fecha = hoy, series = listOf(serie(1, 10, "Sentadilla", 100.0), serie(2, 10, "Sentadilla", 105.0))),
        Entrenamiento(2, 1, fecha = hoy.minusDays(7), series = listOf(serie(3, 10, "Sentadilla", 95.0), serie(4, 20, "Press", 60.0))),
        Entrenamiento(3, 1, fecha = hoy.minusDays(40), series = listOf(serie(5, 20, "Press", 55.0)))
    )

    @Test
    fun `resume totales y entrenos de los ultimos 30 dias`() {
        val resumen = calcularEstadisticas(lista, hoy)

        assertEquals(3, resumen.totalEntrenos)
        assertEquals(5, resumen.totalSeries)
        assertEquals(2, resumen.ultimos30Dias)
        assertEquals(5 * (100.0 + 105.0 + 95.0 + 60.0 + 55.0), resumen.volumenTotal, 0.0)
    }

    @Test
    fun `devuelve ocho semanas terminando en la actual`() {
        val semanas = calcularEstadisticas(lista, hoy).semanas

        assertEquals(SEMANAS_ACTIVIDAD, semanas.size)
        assertEquals(LocalDate.of(2026, 9, 28), semanas.last().first)
        assertEquals(1, semanas.last().second)
        assertEquals(1, semanas[semanas.lastIndex - 1].second)
    }

    @Test
    fun `ordena los ejercicios por numero de series`() {
        val ejercicios = calcularEstadisticas(lista, hoy).ejercicios

        assertEquals(listOf("Sentadilla", "Press"), ejercicios.map { it.nombre })
        assertEquals(3, ejercicios.first().series)
    }

    @Test
    fun `la progresion toma el peso maximo de cada sesion en orden cronologico`() {
        val sentadilla = calcularEstadisticas(lista, hoy).ejercicios.first()
        val progresion = calcularProgresion(lista, sentadilla)

        assertEquals(listOf(95.0, 105.0), progresion.puntos.map { it.pesoMaximo })
        assertEquals(105.0, progresion.record, 0.0)
    }
}
