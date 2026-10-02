package com.mskd.flux.presentation.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.roboto_flex
import org.jetbrains.compose.resources.Font

@Composable
fun rememberRobotoFlex(): FontFamily {
    val weights = listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.Bold)
    val fonts = weights.map { weight ->
        Font(
            resource = Res.font.roboto_flex,
            weight = weight,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight.weight)
            )
        )
    }
    return remember(fonts) { FontFamily(fonts) }
}

@Composable
fun rememberRobotoFlexEmphasized(): FontFamily {
    val font = Font(
        resource = Res.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Bold.weight),
            FontVariation.grade(90),
            FontVariation.width(100f),
            FontVariation.slant(-1f),
            FontVariation.Setting("XOPQ", 60f), // Thick stroke
            FontVariation.Setting("YOPQ", 40f), // Thin stroke
            FontVariation.Setting("YTLC", 500f), // Thin stroke
        ),
    )
    return remember { FontFamily(font) }
}