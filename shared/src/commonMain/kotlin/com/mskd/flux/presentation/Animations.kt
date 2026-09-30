package com.mskd.flux.presentation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
fun animateAlphaState(
    targetValue: Float,
    label: String = "AlphaAnimation",
    finishedListener: ((Float) -> Unit)? = null
): State<Float> {
    return animateFloatAsState(
        targetValue = targetValue,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        label = label,
        finishedListener = finishedListener
    )
}