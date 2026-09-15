package com.iudigital.radio.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * RF-04: preservación del estado ante cambios de configuración.
 *
 * Un [Bitmap] no puede guardarse directamente en el `Bundle` del sistema por su
 * tamaño, de modo que este [Saver] persiste la imagen en el almacenamiento
 * interno de la aplicación y conserva únicamente la ruta del archivo. Al rotar
 * la pantalla la fotografía se vuelve a decodificar desde disco.
 */
private const val PROFILE_PHOTO_FILE = "perfil_usuario.jpg"
private const val NO_PHOTO = ""
private const val JPEG_QUALITY = 85

/**
 * Lado máximo del avatar en píxeles.
 *
 * La cámara del sistema puede devolver imágenes de varios megapíxeles. Mostrar
 * ese mapa de bits como textura y volver a comprimirlo en cada guardado de
 * estado bloquea el hilo principal (se observaron ANR en el emulador), así que
 * la fotografía se reduce en cuanto se recibe: el avatar se dibuja a 96 dp.
 */
private const val MAX_AVATAR_PX = 512

/**
 * Reduce la fotografía capturada a un tamaño razonable para un avatar,
 * conservando la relación de aspecto. Si ya es pequeña se devuelve intacta.
 */
fun Bitmap.scaleForAvatar(maxSide: Int = MAX_AVATAR_PX): Bitmap {
    val longestSide = max(width, height)
    if (longestSide <= maxSide) return this

    val factor = maxSide.toFloat() / longestSide
    val newWidth = (width * factor).roundToInt().coerceAtLeast(1)
    val newHeight = (height * factor).roundToInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
}

private fun bitmapSaver(context: Context): Saver<Bitmap?, String> = Saver(
    save = { bitmap ->
        if (bitmap == null) {
            NO_PHOTO
        } else {
            val file = File(context.filesDir, PROFILE_PHOTO_FILE)
            runCatching {
                file.outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it)
                }
                file.absolutePath
            }.getOrDefault(NO_PHOTO)
        }
    },
    restore = { path ->
        if (path == NO_PHOTO) null else runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
    }
)

/**
 * Estado recordable que sobrevive a la rotación de pantalla y a la muerte del
 * proceso, pensado para la fotografía de perfil capturada con la cámara.
 */
@Composable
fun rememberProfilePhotoState(): MutableState<Bitmap?> {
    val context = LocalContext.current
    return rememberSaveable(stateSaver = bitmapSaver(context)) {
        mutableStateOf(null)
    }
}
