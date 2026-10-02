package com.gympulse.android.data.api

import com.gympulse.android.data.sesion.AlmacenSesion
import okhttp3.Interceptor
import okhttp3.Response

/*
 Toda peticion que sale del movil pasa por aqui. Es el espejo de la cadena de filtros del backend:
 alli se LEE la cabecera Authorization antes de llegar al controlador, y aqui se ESCRIBE antes de
 que la peticion salga, para no tener que pasar el token a mano en cada llamada de GymPulseApi.

 Tambien mira la respuesta: si el backend contesta 401 el token ha caducado o ya no vale, asi que
 se borra la sesion y la interfaz puede reaccionar observando almacen.estado.
*/
class InterceptorToken(private val almacen: AlmacenSesion) : Interceptor {

    override fun intercept(cadena: Interceptor.Chain): Response {
        val token = almacen.token()

        val peticion = if (token == null) {
            cadena.request()
        } else {
            cadena.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }

        val respuesta = cadena.proceed(peticion)

        if (respuesta.code == 401 && token != null) {
            almacen.invalidar()
        }

        return respuesta
    }
}
