package com.mskd.flux.ui.text

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

@Composable
fun fluxTypography(): Typography {
    val family = rememberRobotoFlex()
    return remember(family) {
        Typography(
            displayLarge = displayLarge.copy(fontFamily = family),
            displayMedium = displayMedium.copy(fontFamily = family),
            displaySmall = displaySmall.copy(fontFamily = family),
            headlineLarge = headlineLarge.copy(fontFamily = family),
            headlineMedium = headlineMedium.copy(fontFamily = family),
            headlineSmall = headlineSmall.copy(fontFamily = family),
            titleLarge = titleLarge.copy(fontFamily = family),
            titleMedium = titleMedium.copy(fontFamily = family),
            titleSmall = titleSmall.copy(fontFamily = family),
            bodyLarge = bodyLarge.copy(fontFamily = family),
            bodyMedium = bodyMedium.copy(fontFamily = family),
            bodySmall = bodySmall.copy(fontFamily = family),
            labelLarge = labelLarge.copy(fontFamily = family),
            labelMedium = labelMedium.copy(fontFamily = family),
            labelSmall = labelSmall.copy(fontFamily = family),
        )
    }
}

@Composable
fun fluxEmphasizedTypography(): Typography {
    val family = rememberRobotoFlexEmphasized()
    return remember(family) {
        Typography(
            displayLarge = displayLargeEmphasized.copy(fontFamily = family),
            displayMedium = displayMediumEmphasized.copy(fontFamily = family),
            displaySmall = displaySmallEmphasized.copy(fontFamily = family),
            headlineLarge = headlineLargeEmphasized.copy(fontFamily = family),
            headlineMedium = headlineMediumEmphasized.copy(fontFamily = family),
            headlineSmall = headlineSmallEmphasized.copy(fontFamily = family),
            titleLarge = titleLargeEmphasized.copy(fontFamily = family),
            titleMedium = titleMediumEmphasized.copy(fontFamily = family),
            titleSmall = titleSmallEmphasized.copy(fontFamily = family),
            bodyLarge = bodyLargeEmphasized.copy(fontFamily = family),
            bodyMedium = bodyMediumEmphasized.copy(fontFamily = family),
            bodySmall = bodySmallEmphasized.copy(fontFamily = family),
            labelLarge = labelLargeEmphasized.copy(fontFamily = family),
            labelMedium = labelMediumEmphasized.copy(fontFamily = family),
            labelSmall = labelSmallEmphasized.copy(fontFamily = family),
        )
    }
}

val LocalEmphasizedTypography = compositionLocalOf {
    Typography(
        displayLarge = displayLargeEmphasized,
        displayMedium = displayMediumEmphasized,
        displaySmall = displaySmallEmphasized,
        headlineLarge = headlineLargeEmphasized,
        headlineMedium = headlineMediumEmphasized,
        headlineSmall = headlineSmallEmphasized,
        titleLarge = titleLargeEmphasized,
        titleMedium = titleMediumEmphasized,
        titleSmall = titleSmallEmphasized,
        bodyLarge = bodyLargeEmphasized,
        bodyMedium = bodyMediumEmphasized,
        bodySmall = bodySmallEmphasized,
        labelLarge = labelLargeEmphasized,
        labelMedium = labelMediumEmphasized,
        labelSmall = labelSmallEmphasized,
    )
}