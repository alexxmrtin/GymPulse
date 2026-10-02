package com.gympulse.android.data.preferencias

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStorePreferencias: DataStore<Preferences> by preferencesDataStore(name = "preferencias")

enum class UnidadPeso(val simbolo: String, val factorDesdeKg: Double) {
    KG("kg", 1.0),
    LB("lb", 2.20462);

    fun desdeKg(kg: Double): Double = kg * factorDesdeKg

    fun aKg(valor: Double): Double = valor / factorDesdeKg
}

data class Preferencias(
    val unidadPeso: UnidadPeso = UnidadPeso.KG,
    val recordatorioEntreno: Boolean = false,
    val resumenSemanal: Boolean = false
)

class AlmacenPreferencias(private val context: Context) {

    private val claveUnidad = stringPreferencesKey("unidadPeso")
    private val claveRecordatorio = booleanPreferencesKey("recordatorioEntreno")
    private val claveResumen = booleanPreferencesKey("resumenSemanal")

    val preferencias: Flow<Preferencias> = context.dataStorePreferencias.data.map { guardado ->
        Preferencias(
            unidadPeso = guardado[claveUnidad]
                ?.let { nombre -> UnidadPeso.entries.firstOrNull { it.name == nombre } }
                ?: UnidadPeso.KG,
            recordatorioEntreno = guardado[claveRecordatorio] ?: false,
            resumenSemanal = guardado[claveResumen] ?: false
        )
    }

    suspend fun cambiarUnidad(unidad: UnidadPeso) {
        context.dataStorePreferencias.edit { it[claveUnidad] = unidad.name }
    }

    suspend fun cambiarRecordatorio(activo: Boolean) {
        context.dataStorePreferencias.edit { it[claveRecordatorio] = activo }
    }

    suspend fun cambiarResumenSemanal(activo: Boolean) {
        context.dataStorePreferencias.edit { it[claveResumen] = activo }
    }
}
