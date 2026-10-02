package com.gympulse.android.ui.comun

import com.gympulse.android.data.preferencias.UnidadPeso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormatoTest {

    @Test
    fun `formatea con coma decimal y sin ceros sobrantes`() {
        assertEquals("82,5", formatearNumero(82.5))
        assertEquals("80", formatearNumero(80.0))
    }

    @Test
    fun `el numero editable no agrupa miles`() {
        assertEquals("1250,5", numeroEditable(1250.5))
    }

    @Test
    fun `acepta coma o punto al leer decimales`() {
        assertEquals(62.5, parsearDecimal("62,5")!!, 0.0)
        assertEquals(62.5, parsearDecimal(" 62.5 ")!!, 0.0)
        assertNull(parsearDecimal("abc"))
    }

    @Test
    fun `convierte entre kilos y libras ida y vuelta`() {
        assertEquals("100 kg", formatearPeso(100.0, UnidadPeso.KG))
        assertEquals("220,5 lb", formatearPeso(100.0, UnidadPeso.LB))
        assertEquals(100.0, UnidadPeso.LB.aKg(UnidadPeso.LB.desdeKg(100.0)), 1e-9)
    }

    @Test
    fun `la duracion solo muestra horas cuando las hay`() {
        assertEquals("05:07", formatearDuracion(307))
        assertEquals("1:00:05", formatearDuracion(3605))
    }
}
