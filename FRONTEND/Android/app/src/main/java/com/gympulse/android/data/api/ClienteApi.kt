package com.gympulse.android.data.api

import com.gympulse.android.BuildConfig
import com.gympulse.android.data.sesion.AlmacenSesion
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ClienteApi {

    /*
     10.0.2.2 es la direccion con la que el emulador ve el localhost del ordenador. Desde un movil
     fisico hay que cambiarla por la IP del PC en la red local.
    */
    const val URL_BASE = "http://10.0.2.2:8080/"

    fun crear(almacen: AlmacenSesion): GymPulseApi {
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        val registro = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val cliente = OkHttpClient.Builder()
            .addInterceptor(InterceptorToken(almacen))
            .addInterceptor(registro)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(cliente)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GymPulseApi::class.java)
    }
}
