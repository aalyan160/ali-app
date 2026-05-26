package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonTeal,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    secondary = NeonGreen,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    tertiary = YellowMedium,
    onTertiary = androidx.compose.ui.graphics.Color.Black,
    error = RedError,
    onError = androidx.compose.ui.graphics.Color.White,
    background = BackgroundDark,
    onBackground = OnSurfaceLight,
    surface = SurfaceDark,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = OnSurfaceLight.copy(alpha = 0.7f),
    outline = SlateOutline
)

private val LightColorScheme = lightColorScheme(
    primary = TealLight,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = GreenLight,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = YellowLight,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    error = RedErrorLight,
    onError = androidx.compose.ui.graphics.Color.White,
    background = BackgroundLight,
    onBackground = OnSurfaceDark,
    surface = SurfaceLight,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = SlateOutline
)

val LockedInPurpleTheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFB388FF),
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    secondary = NeonGreen,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    tertiary = YellowMedium,
    onTertiary = androidx.compose.ui.graphics.Color.Black,
    error = RedError,
    onError = androidx.compose.ui.graphics.Color.White,
    background = BackgroundDark,
    onBackground = OnSurfaceLight,
    surface = SurfaceDark,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = OnSurfaceLight.copy(alpha = 0.7f),
    outline = SlateOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    usePurpleTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (usePurpleTheme) LockedInPurpleTheme else if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
