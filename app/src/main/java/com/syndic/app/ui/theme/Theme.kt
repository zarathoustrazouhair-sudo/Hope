package com.syndic.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    secondary = CyanNeon,
    tertiary = RoseNeon,
    background = NightBlue,
    surface = Slate,
    onPrimary = NightBlue,
    onSecondary = NightBlue,
    onTertiary = NightBlue,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun SyndicAppTheme(
    content: @Composable () -> Unit
) {
    // Force Dark Mode always for "Night Cockpit" identity
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
