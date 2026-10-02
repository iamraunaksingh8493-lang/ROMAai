package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RomaCyan,
    onPrimary = Color(0xFF041E28),
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = Color(0xFFA5F3FC),
    secondary = RomaViolet,
    onSecondary = Color(0xFF1E0E4E),
    secondaryContainer = Color(0xFF3B1E78),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = RomaBlue,
    background = RomaDarkBg,
    onBackground = TextPrimaryDark,
    surface = RomaDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = RomaDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF6D28D9),
    tertiary = Color(0xFF0284C7),
    background = RomaLightBg,
    onBackground = Color(0xFF0F172A),
    surface = RomaLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = RomaLightCard,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun RomaTheme(
    darkTheme: Boolean = true, // Default to sleek futuristic dark AI theme
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
