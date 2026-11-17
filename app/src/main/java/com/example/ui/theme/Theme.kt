package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDeep,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = GoldAccent,
    onSecondary = Color.White,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = OnGoldContainer,
    tertiary = TealSecondary,
    onTertiary = Color.White,
    tertiaryContainer = TealContainer,
    onTertiaryContainer = OnTealContainer,
    background = IvoryBackground,
    onBackground = OnSurfaceDark,
    surface = CreamSurface,
    onSurface = OnSurfaceDark,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = OnSurfaceMuted,
    outline = OutlineSubtle,
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = ExpenseContainer,
    onErrorContainer = ExpenseRed
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = EmeraldContainer,
    secondary = DarkGoldAccent,
    onSecondary = Color(0xFF281E00),
    secondaryContainer = Color(0xFF4F3E0D),
    onSecondaryContainer = GoldSoft,
    tertiary = Color(0xFF8CD3BE),
    onTertiary = Color(0xFF00382B),
    background = DarkEmeraldBg,
    onBackground = DarkOnSurface,
    surface = DarkEmeraldSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkEmeraldSurfaceVar,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = Color(0xFF324B42),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF4A0A0A),
    errorContainer = Color(0xFF5D1919),
    onErrorContainer = Color(0xFFFFDAD6)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun OrgAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
