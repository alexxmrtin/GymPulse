package com.gympulse.android.data.perfil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ReductorImagen {

    private const val LADO_MAXIMO = 512
    private const val CALIDAD_JPEG = 85

    fun aJpeg(context: Context, origen: Uri): ByteArray {
        val imagen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            decodificarConImageDecoder(context, origen)
        } else {
            decodificarConBitmapFactory(context, origen)
        }

        return ByteArrayOutputStream().use { salida ->
            imagen.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
            imagen.recycle()
            salida.toByteArray()
        }
    }

    private fun decodificarConImageDecoder(context: Context, origen: Uri): Bitmap {
        val fuente = ImageDecoder.createSource(context.contentResolver, origen)
        return ImageDecoder.decodeBitmap(fuente) { decodificador, info, _ ->
            val escala = min(1f, LADO_MAXIMO / max(info.size.width, info.size.height).toFloat())
            decodificador.setTargetSize(
                (info.size.width * escala).roundToInt().coerceAtLeast(1),
                (info.size.height * escala).roundToInt().coerceAtLeast(1)
            )
            decodificador.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    private fun decodificarConBitmapFactory(context: Context, origen: Uri): Bitmap {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(origen)?.use { BitmapFactory.decodeStream(it, null, limites) }

        var muestreo = 1
        while (max(limites.outWidth, limites.outHeight) / (muestreo * 2) >= LADO_MAXIMO) {
            muestreo *= 2
        }

        val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
        return context.contentResolver.openInputStream(origen)
            ?.use { BitmapFactory.decodeStream(it, null, opciones) }
            ?: error("No se pudo leer la imagen")
    }
}
