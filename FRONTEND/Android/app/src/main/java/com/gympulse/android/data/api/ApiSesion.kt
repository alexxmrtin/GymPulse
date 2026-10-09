package com.gympulse.android.data.api

import com.gympulse.android.data.modelo.RefrescoPeticion
import com.gympulse.android.data.modelo.Sesion
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiSesion {

    @POST("usuario/refresco")
    fun refrescar(@Body datos: RefrescoPeticion): Call<Sesion>

    @POST("usuario/logout")
    suspend fun cerrarSesion(@Body datos: RefrescoPeticion)
}
