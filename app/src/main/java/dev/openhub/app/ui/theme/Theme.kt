package dev.openhub.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CinematicColorScheme = lightColorScheme(
    primary = Color.Black,
    secondary = Color.Black,
    background = WhiteBackground,
    surface = WhiteBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextTitle,
    onSurface = TextTitle,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = TextSubtitle
)

@Composable
fun OpenHubTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CinematicColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val wic = WindowCompat.getInsetsController(window, view)
            wic.isAppearanceLightStatusBars = true
            wic.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
