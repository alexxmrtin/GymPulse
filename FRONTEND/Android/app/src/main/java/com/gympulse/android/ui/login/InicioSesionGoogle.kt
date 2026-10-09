package com.gympulse.android.ui.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.gympulse.android.BuildConfig

sealed interface ResultadoGoogle {
    data class Token(val idToken: String) : ResultadoGoogle
    data object Cancelado : ResultadoGoogle
    data object SinCuentas : ResultadoGoogle
    data class Error(val mensaje: String) : ResultadoGoogle
}

suspend fun pedirTokenDeGoogle(actividad: Context): ResultadoGoogle {
    val opcionGoogle = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_SERVER_CLIENT_ID).build()
    val peticion = GetCredentialRequest.Builder()
        .addCredentialOption(opcionGoogle)
        .build()

    return try {
        val respuesta = CredentialManager.create(actividad).getCredential(actividad, peticion)
        val credencial = respuesta.credential

        if (credencial is CustomCredential && credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            ResultadoGoogle.Token(GoogleIdTokenCredential.createFrom(credencial.data).idToken)
        } else {
            ResultadoGoogle.Error("Google ha devuelto un tipo de credencial inesperado")
        }
    } catch (_: GetCredentialCancellationException) {
        ResultadoGoogle.Cancelado
    } catch (_: NoCredentialException) {
        ResultadoGoogle.SinCuentas
    } catch (e: GetCredentialException) {
        ResultadoGoogle.Error(e.message ?: "No se ha podido iniciar sesión con Google")
    } catch (_: GoogleIdTokenParsingException) {
        ResultadoGoogle.Error("La respuesta de Google no es válida")
    }
}
