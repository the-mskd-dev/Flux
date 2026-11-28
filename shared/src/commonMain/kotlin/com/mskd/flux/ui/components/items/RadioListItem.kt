package com.mskd.flux.ui.components.items

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.motion.Motion
import com.mskd.flux.ui.text.Text

@Composable
fun RadioListItem(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .3f) else Color.Transparent,
        animationSpec = Motion.slowEffects(),
        label = "RadioListItemContainerColor"
    )

    ChoiceItem(
        modifier = modifier,
        text = text,
        selected = selected,
        type = ChoiceType.Radio,
        onClick = onClick,
        containerColor = containerColor,
        textStyle = Text.Style.listBody().copy(
            fontWeight = if (selected) FontWeight.W500 else FontWeight.Normal,
        ),
    ) { indicatorModifier ->
        RadioChip(
            modifier = indicatorModifier,
            selected = selected,
        )
    }

}

@Composable
fun RadioChip(
    modifier: Modifier = Modifier,
    selected: Boolean
) {

    val circleAnim by animateDpAsState(
        targetValue = if (selected) 8.dp else .5.dp,
        animationSpec = Motion.fastEffects(),
        label = "RadioChipAnimation"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(24.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .border(
                    width = circleAnim.coerceAtLeast(0.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )

        )

    }

}

@Preview
@Composable
fun RadioListItem_Preview() {

    var selectedItem by remember { mutableIntStateOf(1) }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(Design.Space.large),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier.padding(horizontal = Design.Space.large),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {

                (0..4).forEach {
                    RadioListItem(
                        text = "Item $it",
                        selected = selectedItem == it,
                        onClick = { selectedItem = it }
                    )
                }

            }

        }

    }

}