package com.mskd.flux.ui.global

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.mskd.flux.ui.motion.Motion

@Composable
fun animateAlphaState(
    targetValue: Float,
    label: String = "AlphaAnimation",
    finishedListener: ((Float) -> Unit)? = null
): State<Float> {
    return animateFloatAsState(
        targetValue = targetValue,
        animationSpec = Motion.slowEffects(),
        label = label,
        finishedListener = finishedListener
    )
}