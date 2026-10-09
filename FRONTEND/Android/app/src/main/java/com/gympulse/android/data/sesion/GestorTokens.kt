package com.gympulse.android.data.sesion

interface GestorTokens {

    fun token(): String?

    fun tokenRefresco(): String?

    fun actualizarTokens(token: String, tokenRefresco: String)

    fun invalidar()
}
