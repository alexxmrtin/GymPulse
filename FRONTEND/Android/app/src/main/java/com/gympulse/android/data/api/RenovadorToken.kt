package com.gympulse.android.data.api

import com.gympulse.android.data.modelo.RefrescoPeticion
import com.gympulse.android.data.sesion.GestorTokens
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException

class RenovadorToken(
    private val almacen: GestorTokens,
    private val apiSesion: ApiSesion
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val tokenUsado = response.request.header(CABECERA)?.removePrefix(PREFIJO) ?: return null
        if (response.priorResponse != null) return null

        synchronized(this) {
            val actual = almacen.token()
            if (actual != null && actual != tokenUsado) {
                return conToken(response.request, actual)
            }

            val tokenRefresco = almacen.tokenRefresco()
            if (tokenRefresco == null) {
                almacen.invalidar()
                return null
            }

            val respuesta = try {
                apiSesion.refrescar(RefrescoPeticion(tokenRefresco)).execute()
            } catch (_: IOException) {
                return null
            }

            val sesion = respuesta.body()
            if (!respuesta.isSuccessful || sesion == null) {
                if (respuesta.code() in 400..499) almacen.invalidar()
                return null
            }

            almacen.actualizarTokens(sesion.token, sesion.tokenRefresco)
            return conToken(response.request, sesion.token)
        }
    }

    private fun conToken(peticion: Request, token: String): Request =
        peticion.newBuilder().header(CABECERA, "$PREFIJO$token").build()

    private companion object {
        const val CABECERA = "Authorization"
        const val PREFIJO = "Bearer "
    }
}
