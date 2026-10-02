package com.gympulse.android.ui.comun

import com.gympulse.android.data.modelo.Entrenamiento
import com.gympulse.android.data.modelo.Serie
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CalculosTest {

    private val lunes = LocalDate.of(2026, 9, 28)

    private fun entreno(id: Long, fecha: LocalDate, series: List<Serie> = emptyList()) =
        Entrenamiento(id = id, usuarioId = 1, fecha = fecha, series = series)

    private fun serie(id: Long, ejercicioId: Long, reps: Int, peso: Double) =
        Serie(id, entrenamientoId = 1, ejercicioId = ejercicioId, ejercicioNombre = "E$ejercicioId", repeticiones = reps, peso = peso)

    @Test
    fun `la racha cuenta semanas seguidas hasta la actual`() {
        val lista = listOf(
            entreno(1, lunes.plusDays(2)),
            entreno(2, lunes.minusDays(3)),
            entreno(3, lunes.minusWeeks(2))
        )

        assertEquals(3, lista.rachaSemanas(hoy = lunes.plusDays(4)))
    }

    @Test
    fun `la racha no se rompe si aun no se ha entrenado esta semana`() {
        val lista = listOf(entreno(1, lunes.minusDays(1)), entreno(2, lunes.minusDays(8)))

        assertEquals(2, lista.rachaSemanas(hoy = lunes))
    }

    @Test
    fun `la racha es cero si la ultima semana entrenada queda lejos`() {
        assertEquals(0, listOf(entreno(1, lunes.minusWeeks(3))).rachaSemanas(hoy = lunes))
    }

    @Test
    fun `agrupa las series por ejercicio respetando el orden de creacion`() {
        val grupos = listOf(
            serie(3, ejercicioId = 20, reps = 8, peso = 40.0),
            serie(1, ejercicioId = 10, reps = 10, peso = 60.0),
            serie(2, ejercicioId = 10, reps = 8, peso = 65.0)
        ).agruparPorEjercicio()

        assertEquals(listOf(10L, 20L), grupos.map { it.ejercicioId })
        assertEquals(listOf(1L, 2L), grupos.first().series.map { it.id })
        assertEquals(65.0, grupos.first().pesoMaximo, 0.0)
    }

    @Test
    fun `el volumen suma repeticiones por peso`() {
        val entrenamiento = entreno(1, lunes, listOf(serie(1, 10, 10, 50.0), serie(2, 10, 5, 100.0)))

        assertEquals(1000.0, entrenamiento.volumen, 0.0)
        assertEquals(1, entrenamiento.numeroEjercicios)
    }
}
