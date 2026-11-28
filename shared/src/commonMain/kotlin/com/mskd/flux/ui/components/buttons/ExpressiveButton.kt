package com.mskd.flux.ui.components.buttons

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.motion.Motion
import com.mskd.flux.ui.motion.expressiveBounceOnChange
import com.mskd.flux.ui.motion.expressiveContentSize
import com.mskd.flux.ui.motion.expressivePress

private const val INACTIVE_CORNER_PERCENT = 50
private const val ACTIVE_CORNER_PERCENT = 30

@Composable
internal fun ExpressiveButton(
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    contentSpacing: Dp = Design.Space.extraSmall,
    content: @Composable RowScope.() -> Unit,
) {
    val cornerPercent by animateIntAsState(
        targetValue = if (active) ACTIVE_CORNER_PERCENT else INACTIVE_CORNER_PERCENT,
        animationSpec = Motion.fastSpatial(),
        label = "ExpressiveButtonCorner"
    )

    val animatedContainerColor by animateColorAsState(
        targetValue = containerColor,
        animationSpec = Motion.defaultEffects(),
        label = "ExpressiveButtonContainerColor"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = contentColor,
        animationSpec = Motion.defaultEffects(),
        label = "ExpressiveButtonContentColor"
    )

    FilledTonalButton(
        modifier = modifier
            .expressivePress(enabled = enabled)
            .expressiveBounceOnChange(state = active),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(percent = cornerPercent.coerceIn(0, 100)),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = animatedContainerColor,
            contentColor = animatedContentColor,
            disabledContainerColor = animatedContainerColor,
            disabledContentColor = animatedContentColor,
        ),
        contentPadding = contentPadding,
    ) {
        Row(
            modifier = Modifier.expressiveContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(contentSpacing),
            content = content,
        )
    }
}
