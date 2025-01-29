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
import com.kaem.flux.utils.extensions.logDescription

private val FluxColorScheme = darkColorScheme(
    primary = Color(0.827451f, 0.73333335f, 1.0f), // #D3BBFF
    onPrimary = Color(0.23529412f, 0.11372549f, 0.4392157f), // #3C1D70
    primaryContainer = Color(0.3254902f, 0.21176471f, 0.53333336f), // #533688
    onPrimaryContainer = Color(0.92156863f, 0.8627451f, 1.0f), // #EBDCFF
    inversePrimary = Color(0.41960785f, 0.30588236f, 0.63529414f), // #6B4EA2
    secondary = Color(0.8039216f, 0.7607843f, 0.85882354f), // #CDC2DB
    onSecondary = Color(0.20392157f, 0.1764706f, 0.2509804f), // #342D40
    secondaryContainer = Color(0.29411766f, 0.2627451f, 0.34509805f), // #4B4358
    onSecondaryContainer = Color(0.91764706f, 0.87058824f, 0.972549f), // #EADEF8
    tertiary = Color(0.94509804f, 0.7176471f, 0.77254903f), // #F1B7C5
    onTertiary = Color(0.2901961f, 0.14509805f, 0.1882353f), // #4A2530
    tertiaryContainer = Color(0.39215687f, 0.23137255f, 0.27450982f), // #643B46
    onTertiaryContainer = Color(1.0f, 0.8509804f, 0.88235295f), // #FFD9E1
    background = Color(0.07450981f, 0.07450981f, 0.07450981f), // #131313
    onBackground = Color(0.8862745f, 0.8862745f, 0.8862745f), // #E2E2E2
    surface = Color(0.07450981f, 0.07450981f, 0.07450981f), // #131313
    onSurface = Color(0.8862745f, 0.8862745f, 0.8862745f), // #E2E2E2
    surfaceVariant = Color(0.2784314f, 0.2784314f, 0.2784314f), // #474747
    onSurfaceVariant = Color(0.7764706f, 0.7764706f, 0.7764706f), // #C6C6C6
    surfaceTint = Color(0.827451f, 0.73333335f, 1.0f), // #D3BBFF
    inverseSurface = Color(0.9764706f, 0.9764706f, 0.9764706f), // #F9F9F9
    inverseOnSurface = Color(0.105882354f, 0.105882354f, 0.105882354f), // #1B1B1B
    error = Color(0.9490196f, 0.72156864f, 0.70980394f), // #F2B8B5
    onError = Color(0.3764706f, 0.078431375f, 0.0627451f), // #601410
    errorContainer = Color(0.54901963f, 0.11372549f, 0.09411765f), // #8C1D18
    onErrorContainer = Color(0.9764706f, 0.87058824f, 0.8627451f), // #F9DEDC
    outline = Color(0.5686275f, 0.5686275f, 0.5686275f), // #919191
    outlineVariant = Color(0.2784314f, 0.2784314f, 0.2784314f), // #474747
    scrim = Color(0.0f, 0.0f, 0.0f), // #000000
    surfaceBright = Color(0.22352941f, 0.22352941f, 0.22352941f), // #393939
    surfaceDim = Color(0.07450981f, 0.07450981f, 0.07450981f), // #131313
    surfaceContainer = Color(0.12156863f, 0.12156863f, 0.12156863f), // #1F1F1F
    surfaceContainerHigh = Color(0.16470589f, 0.16470589f, 0.16470589f), // #2A2A2A
    surfaceContainerHighest = Color(0.20784314f, 0.20784314f, 0.20784314f), // #353535
    surfaceContainerLow = Color(0.105882354f, 0.105882354f, 0.105882354f), // #1B1B1B
    surfaceContainerLowest = Color(0.05490196f, 0.05490196f, 0.05490196f), // #0E0E0E
)

private val FluxLightColorScheme = lightColorScheme(
    primary = Color(0.41960785f, 0.30588236f, 0.63529414f), // #6B4EA2
    onPrimary = Color(1.0f, 1.0f, 1.0f), // #FFFFFF
    primaryContainer = Color(0.92156863f, 0.8627451f, 1.0f), // #EBDCFF
    onPrimaryContainer = Color(0.14901961f, 0.0f, 0.34901962f), // #260059
    inversePrimary = Color(0.827451f, 0.73333335f, 1.0f), // #D3BBFF
    secondary = Color(0.3882353f, 0.35686275f, 0.4392157f), // #635B70
    onSecondary = Color(1.0f, 1.0f, 1.0f), // #FFFFFF
    secondaryContainer = Color(0.91764706f, 0.87058824f, 0.972549f), // #EADEF8
    onSecondaryContainer = Color(0.12156863f, 0.09411765f, 0.16862746f), // #1F182B
    tertiary = Color(0.49803922f, 0.32156864f, 0.3647059f), // #7F525D
    onTertiary = Color(1.0f, 1.0f, 1.0f), // #FFFFFF
    tertiaryContainer = Color(1.0f, 0.8509804f, 0.88235295f), // #FFD9E1
    onTertiaryContainer = Color(0.19607843f, 0.0627451f, 0.105882354f), // #32101B
    background = Color(0.9764706f, 0.9764706f, 0.9764706f), // #F9F9F9
    onBackground = Color(0.105882354f, 0.105882354f, 0.105882354f), // #1B1B1B
    surface = Color(0.9764706f, 0.9764706f, 0.9764706f), // #F9F9F9
    onSurface = Color(0.105882354f, 0.105882354f, 0.105882354f), // #1B1B1B
    surfaceVariant = Color(0.8862745f, 0.8862745f, 0.8862745f), // #E2E2E2
    onSurfaceVariant = Color(0.2784314f, 0.2784314f, 0.2784314f), // #474747
    surfaceTint = Color(0.41960785f, 0.30588236f, 0.63529414f), // #6B4EA2
    inverseSurface = Color(0.07450981f, 0.07450981f, 0.07450981f), // #131313
    inverseOnSurface = Color(0.8862745f, 0.8862745f, 0.8862745f), // #E2E2E2
    error = Color(0.7019608f, 0.14901961f, 0.11764706f), // #B3261E
    onError = Color(1.0f, 1.0f, 1.0f), // #FFFFFF
    errorContainer = Color(0.9764706f, 0.87058824f, 0.8627451f), // #F9DEDC
    onErrorContainer = Color(0.25490198f, 0.05490196f, 0.043137256f), // #410E0B
    outline = Color(0.46666667f, 0.46666667f, 0.46666667f), // #777777
    outlineVariant = Color(0.7764706f, 0.7764706f, 0.7764706f), // #C6C6C6
    scrim = Color(0.0f, 0.0f, 0.0f), // #000000
    surfaceBright = Color(0.9764706f, 0.9764706f, 0.9764706f), // #F9F9F9
    surfaceDim = Color(0.85490197f, 0.85490197f, 0.85490197f), // #DADADA
    surfaceContainer = Color(0.93333334f, 0.93333334f, 0.93333334f), // #EEEEEE
    surfaceContainerHigh = Color(0.9098039f, 0.9098039f, 0.9098039f), // #E8E8E8
    surfaceContainerHighest = Color(0.8862745f, 0.8862745f, 0.8862745f), // #E2E2E2
    surfaceContainerLow = Color(0.9529412f, 0.9529412f, 0.9529412f), // #F3F3F3
    surfaceContainerLowest = Color(1.0f, 1.0f, 1.0f), // #FFFFFF
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

    colorScheme.logDescription()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}