package com.gympulse.android.data.perfil

import android.content.Context
import android.net.Uri
import com.gympulse.android.data.api.ClienteApi
import com.gympulse.android.data.api.GymPulseApi
import com.gympulse.android.data.modelo.Usuario
import com.gympulse.android.data.sesion.AlmacenSesion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class FotoPerfilRepositorio(
    private val context: Context,
    private val api: GymPulseApi,
    private val almacen: AlmacenSesion
) {

    suspend fun subir(origen: Uri): Result<Usuario> = runCatching {
        val id = almacen.usuarioId ?: error("No hay sesion iniciada")
        val jpeg = withContext(Dispatchers.IO) { ReductorImagen.aJpeg(context, origen) }
        val parte = MultipartBody.Part.createFormData(
            name = "foto",
            filename = "foto.jpg",
            body = jpeg.toRequestBody("image/jpeg".toMediaType())
        )
        api.subirFoto(id, parte)
    }

    suspend fun quitar(): Result<Unit> = runCatching {
        val id = almacen.usuarioId ?: error("No hay sesion iniciada")
        api.eliminarFoto(id)
    }
}

fun Usuario.urlFoto(): String? =
    fotoActualizadaEn?.let { version -> "${ClienteApi.URL_BASE}usuario/$id/foto?v=${Uri.encode(version)}" }
