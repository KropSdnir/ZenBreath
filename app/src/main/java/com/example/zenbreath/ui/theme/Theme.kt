package com.example.zenbreath.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ZenGreenSecondary,
    secondary = ZenGreenTertiary,
    tertiary = ZenGreenPrimary,
    error = ZenRedError,
    errorContainer = ZenRedError,
    onErrorContainer = Color.White,
    background = Color(0xFF101310), // Deep Zen Dark
    surface = Color(0xFF101310),
    onSurface = Color.White,
    outline = ZenOutline
)

private val LightColorScheme = lightColorScheme(
    primary = ZenGreenPrimary,
    secondary = ZenGreenSecondary,
    tertiary = ZenGreenTertiary,
    error = ZenRedError,
    errorContainer = ZenRedContainer,
    onErrorContainer = ZenRedError,
    background = ZenSurface,
    surface = ZenSurface,
    onSurface = ZenOnSurface,
    outline = ZenOutline
)

@Composable
fun ZenBreathTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to maintain our custom Zen branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}