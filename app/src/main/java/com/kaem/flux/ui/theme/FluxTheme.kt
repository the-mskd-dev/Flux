package com.kaem.flux.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FluxColorScheme = darkColorScheme(
    primary = Color(0.9882353f, 0.6901961f, 0.8392157f),
    onPrimary = Color(0.31764707f, 0.11372549f, 0.23529412f),
    primaryContainer = Color(0.42352942f, 0.20392157f, 0.32941177f),
    onPrimaryContainer = Color(1.0f, 0.84705883f, 0.9137255f),
    inversePrimary = Color(0.5294118f, 0.29411766f, 0.42352942f),
    secondary = Color(0.8745098f, 0.7411765f, 0.8f),
    onSecondary = Color(0.2509804f, 0.16470589f, 0.20784314f),
    secondaryContainer = Color(0.34509805f, 0.2509804f, 0.29803923f),
    onSecondaryContainer = Color(0.99215686f, 0.8509804f, 0.9098039f),
    tertiary = Color(0.9529412f, 0.7294118f, 0.60784316f),
    onTertiary = Color(0.2901961f, 0.15686275f, 0.07058824f),
    tertiaryContainer = Color(0.39215687f, 0.23921569f, 0.14901961f),
    onTertiaryContainer = Color(1.0f, 0.85882354f, 0.7882353f),
    background = Color(0.09411765f, 0.06666667f, 0.08235294f),
    onBackground = Color(0.93333334f, 0.8745098f, 0.8901961f),
    surface = Color(0.09411765f, 0.06666667f, 0.08235294f),
    onSurface = Color(0.93333334f, 0.8745098f, 0.8901961f),
    surfaceVariant = Color(0.3137255f, 0.2627451f, 0.28627452f),
    onSurfaceVariant = Color(0.83137256f, 0.7607843f, 0.78431374f),
    surfaceTint = Color(0.9882353f, 0.6901961f, 0.8392157f),
    inverseSurface = Color(1.0f, 0.972549f, 0.972549f),
    inverseOnSurface = Color(0.12941177f, 0.101960786f, 0.11372549f),
    error = Color(0.9490196f, 0.72156864f, 0.70980394f),
    onError = Color(0.3764706f, 0.078431375f, 0.0627451f),
    errorContainer = Color(0.54901963f, 0.11372549f, 0.09411765f),
    onErrorContainer = Color(0.9764706f, 0.87058824f, 0.8627451f),
    outline = Color(0.6117647f, 0.5529412f, 0.5764706f),
    outlineVariant = Color(0.3137255f, 0.2627451f, 0.28627452f),
    scrim = Color(0.0f, 0.0f, 0.0f),
    surfaceBright = Color(0.2509804f, 0.21568628f, 0.22745098f),
    surfaceDim = Color(0.09411765f, 0.06666667f, 0.08235294f),
    surfaceContainer = Color(0.14509805f, 0.11372549f, 0.12941177f),
    surfaceContainerHigh = Color(0.1882353f, 0.15686275f, 0.16862746f),
    surfaceContainerHighest = Color(0.23137255f, 0.19607843f, 0.21176471f),
    surfaceContainerLow = Color(0.12941177f, 0.101960786f, 0.11372549f),
    surfaceContainerLowest = Color(0.07450981f, 0.047058824f, 0.05882353f)
)

private val FluxLightColorScheme = lightColorScheme(
    primary = Color(0.5294118f, 0.29411766f, 0.42352942f),
    onPrimary = Color(1.0f, 1.0f, 1.0f),
    primaryContainer = Color(1.0f, 0.84705883f, 0.9137255f),
    onPrimaryContainer = Color(0.21960784f, 0.02745098f, 0.14901961f),
    inversePrimary = Color(0.9882353f, 0.6901961f, 0.8392157f),
    secondary = Color(0.44705883f, 0.34117648f, 0.3882353f),
    onSecondary = Color(1.0f, 1.0f, 1.0f),
    secondaryContainer = Color(0.99215686f, 0.8509804f, 0.9098039f),
    onSecondaryContainer = Color(0.16078432f, 0.08235294f, 0.1254902f),
    tertiary = Color(0.49803922f, 0.32941177f, 0.23137255f),
    onTertiary = Color(1.0f, 1.0f, 1.0f),
    tertiaryContainer = Color(1.0f, 0.85882354f, 0.7882353f),
    onTertiaryContainer = Color(0.19215687f, 0.07450981f, 0.007843138f),
    background = Color(1.0f, 0.972549f, 0.972549f),
    onBackground = Color(0.12941177f, 0.101960786f, 0.11372549f),
    surface = Color(1.0f, 0.972549f, 0.972549f),
    onSurface = Color(0.12941177f, 0.101960786f, 0.11372549f),
    surfaceVariant = Color(0.9411765f, 0.87058824f, 0.89411765f),
    onSurfaceVariant = Color(0.3137255f, 0.2627451f, 0.28627452f),
    surfaceTint = Color(0.5294118f, 0.29411766f, 0.42352942f),
    inverseSurface = Color(0.09411765f, 0.06666667f, 0.08235294f),
    inverseOnSurface = Color(0.93333334f, 0.8745098f, 0.8901961f),
    error = Color(0.7019608f, 0.14901961f, 0.11764706f),
    onError = Color(1.0f, 1.0f, 1.0f),
    errorContainer = Color(0.9764706f, 0.87058824f, 0.8627451f),
    onErrorContainer = Color(0.25490198f, 0.05490196f, 0.043137256f),
    outline = Color(0.50980395f, 0.4509804f, 0.4745098f),
    outlineVariant = Color(0.83137256f, 0.7607843f, 0.78431374f),
    scrim = Color(0.0f, 0.0f, 0.0f),
    surfaceBright = Color(1.0f, 0.972549f, 0.972549f),
    surfaceDim = Color(0.8980392f, 0.8392157f, 0.85882354f),
    surfaceContainer = Color(0.9764706f, 0.91764706f, 0.9372549f),
    surfaceContainerHigh = Color(0.9529412f, 0.89411765f, 0.9137255f),
    surfaceContainerHighest = Color(0.93333334f, 0.8745098f, 0.8901961f),
    surfaceContainerLow = Color(1.0f, 0.9411765f, 0.95686275f),
    surfaceContainerLowest = Color(1.0f, 1.0f, 1.0f)
)

@Composable
fun FluxTheme(
    theme: Ui.THEME = Ui.THEME.SYSTEM,
    content: @Composable () -> Unit
) {

    val colorScheme = when (theme) {
        Ui.THEME.LIGHT -> FluxLightColorScheme
        Ui.THEME.DARK -> FluxColorScheme
        Ui.THEME.SYSTEM -> {

            val isDark = isSystemInDarkTheme()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDark) FluxColorScheme else FluxLightColorScheme
            }

        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {

        val transparent = Color(0x00000000)

        SideEffect {

            val window = (view.context as Activity).window
            window.statusBarColor = transparent.toArgb()
            window.navigationBarColor = transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false//darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}