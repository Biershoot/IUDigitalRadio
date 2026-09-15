package com.iudigital.radio.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * RF-05: retroalimentación háptica.
 *
 * Encapsula el acceso al servicio de vibración del sistema resolviendo la
 * diferencia de API entre [VibratorManager] (Android 12 / API 31 en adelante) y
 * el antiguo servicio [Vibrator].
 */
object Haptics {

    private const val TICK_MS = 40L
    private const val DOUBLE_TICK_MS = 25L

    /** Pulsación háptica corta para los botones Play y Pause. */
    fun tick(context: Context) = vibrate(context, longArrayOf(0, TICK_MS))

    /** Patrón doble, ligeramente distinto, para el botón Mute. */
    fun doubleTick(context: Context) =
        vibrate(context, longArrayOf(0, DOUBLE_TICK_MS, 60, DOUBLE_TICK_MS))

    /** Patrón ascendente usado al cambiar de emisora en la lista. */
    fun stationChange(context: Context) = vibrate(context, longArrayOf(0, 15, 40, 35))

    private fun vibrate(context: Context, pattern: LongArray) {
        val vibrator = obtainVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun obtainVibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
}
