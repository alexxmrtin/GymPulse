package com.gympulse.android.data.api

import com.gympulse.android.data.sesion.GestorTokens
import okhttp3.Interceptor
import okhttp3.Response

/*
 Toda peticion que sale del movil pasa por aqui. Es el espejo de la cadena de filtros del backend:
 alli se LEE la cabecera Authorization antes de llegar al controlador, y aqui se ESCRIBE antes de
 que la peticion salga, para no tener que pasar el token a mano en cada llamada de GymPulseApi.
*/
class InterceptorToken(private val almacen: GestorTokens) : Interceptor {

    override fun intercept(cadena: Interceptor.Chain): Response {
        val token = almacen.token()

        val peticion = if (token == null) {
            cadena.request()
        } else {
            cadena.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }

        return cadena.proceed(peticion)
    }
}
