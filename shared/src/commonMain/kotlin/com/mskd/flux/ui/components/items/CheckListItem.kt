package com.mskd.flux.ui.components.items

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.motion.Motion

@Composable
fun CheckListItem(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ChoiceItem(
        modifier = modifier,
        text = text,
        selected = selected,
        type = ChoiceType.Checkbox,
        onClick = onClick,
    ) { indicatorModifier ->
        CheckCase(
            modifier = indicatorModifier,
            selected = selected,
        )
    }
}

@Composable
fun CheckCase(
    modifier: Modifier = Modifier,
    selected: Boolean
) {

    val shape = RoundedCornerShape(percent = 40)

    val boxAnim by animateDpAsState(
        targetValue = if (selected) 16.dp else 0.dp,
        animationSpec = Motion.fastSpatial(),
        label = "CheckCaseAnimation"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .size(24.dp)
            .border(
                width = .5.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = shape
            )
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .clip(shape)
                .size(boxAnim.coerceAtLeast(0.dp))
                .background(MaterialTheme.colorScheme.primary)
        )

    }

}

@Preview
@Composable
fun CheckListItem_Preview() {

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
                    CheckListItem(
                        text = "Item $it",
                        selected = selectedItem == it,
                        onClick = { selectedItem = it }
                    )
                }

            }

        }

    }

}