package com.iudigital.radio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = IuGreen,
    onPrimary = IuNavy,
    secondary = IuGreenSoft,
    onSecondary = IuNavy,
    tertiary = IuAmber,
    background = IuNavy,
    onBackground = IuTextPrimary,
    surface = IuNavySurface,
    onSurface = IuTextPrimary,
    surfaceVariant = IuNavyElevated,
    onSurfaceVariant = IuTextSecondary,
    error = IuRed,
    outline = IuTextSecondary
)

/**
 * La aplicación usa una identidad visual oscura fija (estilo consola de radio)
 * tanto en modo claro como oscuro del sistema, para mantener la marca.
 */
private val LightColors = DarkColors

@Composable
fun IUDigitalRadioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = IuTypography,
        content = content
    )
}
