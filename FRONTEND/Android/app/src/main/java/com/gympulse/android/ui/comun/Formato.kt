package com.gympulse.android.ui.comun

import com.gympulse.android.data.preferencias.UnidadPeso
import java.text.NumberFormat
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

val LOCALE_ES: Locale = Locale.forLanguageTag("es-ES")

private val FORMATO_FECHA_LARGA = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", LOCALE_ES)
private val FORMATO_FECHA_MEDIA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", LOCALE_ES)
private val FORMATO_FECHA_CORTA = DateTimeFormatter.ofPattern("d MMM", LOCALE_ES)
private val FORMATO_MES = DateTimeFormatter.ofPattern("LLLL yyyy", LOCALE_ES)

fun String.capitalizar(): String = replaceFirstChar { it.titlecase(LOCALE_ES) }

fun formatearNumero(valor: Double, maxDecimales: Int = 1, agrupar: Boolean = true): String =
    NumberFormat.getNumberInstance(LOCALE_ES).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = maxDecimales
        isGroupingUsed = agrupar
    }.format(valor)

fun numeroEditable(valor: Double, maxDecimales: Int = 2): String =
    formatearNumero(valor, maxDecimales, agrupar = false)

fun formatearPeso(kg: Double, unidad: UnidadPeso): String =
    "${formatearNumero(unidad.desdeKg(kg))} ${unidad.simbolo}"

fun formatearVolumen(kg: Double, unidad: UnidadPeso): String =
    "${formatearNumero(unidad.desdeKg(kg), maxDecimales = 0)} ${unidad.simbolo}"

fun LocalDate.fechaLarga(): String = format(FORMATO_FECHA_LARGA).capitalizar()

fun LocalDate.fechaMedia(): String = format(FORMATO_FECHA_MEDIA)

fun LocalDate.fechaCorta(): String = format(FORMATO_FECHA_CORTA).trimEnd('.')

fun YearMonth.nombreLargo(): String = format(FORMATO_MES).capitalizar()

fun LocalDate.fechaRelativa(hoy: LocalDate = LocalDate.now()): String = when (this) {
    hoy -> "Hoy"
    hoy.minusDays(1) -> "Ayer"
    else -> fechaLarga()
}

fun edadDesde(fechaNacimiento: LocalDate, hoy: LocalDate = LocalDate.now()): Int =
    Period.between(fechaNacimiento, hoy).years

fun parsearDecimal(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

fun formatearDuracion(segundos: Long): String {
    val horas = segundos / 3600
    val minutos = (segundos % 3600) / 60
    val resto = segundos % 60
    return if (horas > 0) "%d:%02d:%02d".format(horas, minutos, resto) else "%02d:%02d".format(minutos, resto)
}
