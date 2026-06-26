package com.mskd.flux.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.color_blue
import flux.shared.generated.resources.color_gray
import flux.shared.generated.resources.color_green
import flux.shared.generated.resources.color_magenta
import flux.shared.generated.resources.color_red
import flux.shared.generated.resources.color_yellow
import flux.shared.generated.resources.dark
import flux.shared.generated.resources.light
import flux.shared.generated.resources.system
import org.jetbrains.compose.resources.StringResource

object UiCommon {

    enum class THEME {
        LIGHT, DARK, SYSTEM;
        
        val stringResource get() = when(this) {
            LIGHT -> Res.string.light
            DARK -> Res.string.dark
            SYSTEM -> Res.string.system
        }
    }

    sealed class AccentColors(val color: Color?, val stringResId: StringResource) {
        data object System : AccentColors(color = null, stringResId = Res.string.system)
        data object Red : AccentColors(color = Color(239, 71, 111), stringResId = Res.string.color_red)
        data object Blue : AccentColors(color = Color(17, 138, 178), stringResId = Res.string.color_blue)
        data object Green : AccentColors(color = Color(6, 214, 160), stringResId = Res.string.color_green)
        data object Yellow : AccentColors(color = Color(255, 209, 102), stringResId = Res.string.color_yellow)
        data object Magenta : AccentColors(color = Color(181, 23, 158), stringResId = Res.string.color_magenta)
        data object Gray : AccentColors(color = Color(94, 100, 114), stringResId = Res.string.color_gray)

        companion object {

            fun findColor(rgb: Int?) : AccentColors? {
                return when (rgb) {
                    null -> System
                    Red.color?.toArgb() -> Red
                    Blue.color?.toArgb() -> Blue
                    Green.color?.toArgb() -> Green
                    Yellow.color?.toArgb() -> Yellow
                    Magenta.color?.toArgb() -> Magenta
                    Gray.color?.toArgb() -> Gray
                    else -> null
                }
            }
        }
    }

}