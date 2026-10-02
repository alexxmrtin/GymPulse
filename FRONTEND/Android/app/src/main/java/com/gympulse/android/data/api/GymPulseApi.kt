package com.gympulse.android.data.api

import com.gympulse.android.data.modelo.ActualizarUsuarioPeticion
import com.gympulse.android.data.modelo.CrearEjercicioPeticion
import com.gympulse.android.data.modelo.CrearEntrenamientoPeticion
import com.gympulse.android.data.modelo.CrearSeriePeticion
import com.gympulse.android.data.modelo.Ejercicio
import com.gympulse.android.data.modelo.Entrenamiento
import com.gympulse.android.data.modelo.LoginPeticion
import com.gympulse.android.data.modelo.RegistroPeticion
import com.gympulse.android.data.modelo.Serie
import com.gympulse.android.data.modelo.Sesion
import com.gympulse.android.data.modelo.Usuario
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface GymPulseApi {

    @POST("usuario")
    suspend fun registrar(@Body datos: RegistroPeticion): Usuario

    @POST("usuario/login")
    suspend fun login(@Body credenciales: LoginPeticion): Sesion

    @GET("usuario/{id}")
    suspend fun usuario(@Path("id") id: Long): Usuario

    @PUT("usuario/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Long, @Body datos: ActualizarUsuarioPeticion): Usuario

    @DELETE("usuario/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Long)

    @Multipart
    @PUT("usuario/{id}/foto")
    suspend fun subirFoto(@Path("id") id: Long, @Part foto: MultipartBody.Part): Usuario

    @DELETE("usuario/{id}/foto")
    suspend fun eliminarFoto(@Path("id") id: Long)

    @POST("ejercicio")
    suspend fun crearEjercicio(@Body datos: CrearEjercicioPeticion): Ejercicio

    @GET("ejercicio")
    suspend fun ejercicios(
        @Query("nombre") nombre: String? = null,
        @Query("grupoMuscular") grupoMuscular: String? = null
    ): List<Ejercicio>

    @GET("ejercicio/{id}")
    suspend fun ejercicio(@Path("id") id: Long): Ejercicio

    @DELETE("ejercicio/{id}")
    suspend fun eliminarEjercicio(@Path("id") id: Long)

    @POST("entrenamiento")
    suspend fun crearEntrenamiento(@Body datos: CrearEntrenamientoPeticion): Entrenamiento

    @GET("entrenamiento")
    suspend fun misEntrenamientos(): List<Entrenamiento>

    @GET("entrenamiento/{id}")
    suspend fun entrenamiento(@Path("id") id: Long): Entrenamiento

    @DELETE("entrenamiento/{id}")
    suspend fun eliminarEntrenamiento(@Path("id") id: Long)

    @POST("serie")
    suspend fun crearSerie(@Body datos: CrearSeriePeticion): Serie

    @GET("serie")
    suspend fun seriesDeEntrenamiento(@Query("entrenamientoId") entrenamientoId: Long): List<Serie>

    @GET("serie")
    suspend fun seriesDeEjercicio(@Query("ejercicioId") ejercicioId: Long): List<Serie>

    @DELETE("serie/{id}")
    suspend fun eliminarSerie(@Path("id") id: Long)
}
