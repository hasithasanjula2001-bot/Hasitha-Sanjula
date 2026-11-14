package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val MeridianDarkColorScheme = darkColorScheme(
    primary = SovereignGold,
    onPrimary = MidnightNavy,
    primaryContainer = SovereignGoldMuted,
    onPrimaryContainer = SovereignGold,
    secondary = SteelCyan,
    onSecondary = MidnightNavy,
    secondaryContainer = SteelCyanMuted,
    onSecondaryContainer = SteelCyan,
    tertiary = VerifiedEmerald,
    onTertiary = MidnightNavy,
    tertiaryContainer = VerifiedEmeraldMuted,
    onTertiaryContainer = VerifiedEmerald,
    error = SanctionCrimson,
    onError = IceWhite,
    errorContainer = SanctionCrimsonMuted,
    onErrorContainer = SanctionCrimson,
    background = MidnightNavy,
    onBackground = IceWhite,
    surface = DeepNavy,
    onSurface = IceWhite,
    surfaceVariant = SlateNavy,
    onSurfaceVariant = MutedSlate,
    surfaceContainerLowest = MidnightNavy,
    surfaceContainerLow = DeepNavy,
    surfaceContainer = SlateNavy,
    surfaceContainerHigh = ElevatedNavy,
    surfaceContainerHighest = HighlightNavy,
    outline = HairlineSlate,
    outlineVariant = HairlineSlate
)

private val MeridianShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MeridianLcTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = MeridianDarkColorScheme,
        typography = AppTypography,
        shapes = MeridianShapes,
        content = content
    )
}
