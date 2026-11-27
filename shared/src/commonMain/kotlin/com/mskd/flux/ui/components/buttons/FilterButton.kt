package com.mskd.flux.ui.components.buttons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.ui.motion.Motion
import com.mskd.flux.ui.text.Text
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_sort
import org.jetbrains.compose.resources.painterResource

@Composable
fun FilterButton(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    text: String? = null,
    count: Int = 0,
    onClick: () -> Unit,
    selected: Boolean = false,
    contentDescription: String? = null
) {

    require(text != null || contentDescription != null) {
        "FilterButton needs either text or contentDescription"
    }

    val active = selected || count > 0

    var displayedCount by remember { mutableIntStateOf(count) }
    LaunchedEffect(count) { if (count > 0) displayedCount = count }

    val itemEnter = Motion.buttonContentEnter()

    val containerColor = if (active) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = if (active) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    ExpressiveButton(
        modifier = modifier.semantics { this.selected = selected },
        onClick = onClick,
        active = active,
        containerColor = containerColor,
        contentColor = contentColor,
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = itemEnter,
            exit = ExitTransition.None
        ) {
            Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        }

        Crossfade(targetState = painter, label = "icon") { p ->
            p?.let {
                Icon(
                    painter = it,
                    contentDescription = if (text == null) contentDescription else null
                )
            }
        }

        text?.let {
            Text.Button.Chip(
                text = it
            )
        }

        AnimatedVisibility(
            visible = count > 0,
            enter = itemEnter,
            exit = ExitTransition.None
        ) {
            Text.Button.Chip(
                text = "($displayedCount)"
            )
        }
    }
}

@Preview(name = "Icon")
@Composable
fun FilterButton_Icon_Preview() {
    MaterialTheme {
        FilterButton(
            onClick = {},
            painter = painterResource(Res.drawable.ic_sort),
            contentDescription = "icon"
        )
    }
}

@Preview(name = "Text")
@Composable
fun FilterButton_Text_Preview() {
    MaterialTheme {
        FilterButton(
            onClick = {},
            text = "Filter",
            contentDescription = "text"
        )
    }
}

@Preview(name = "Text + Count")
@Composable
fun FilterButton_TextCount_Preview() {
    MaterialTheme {
        FilterButton(
            onClick = {},
            text = "Filter",
            count = 3,
            contentDescription = "text + count"
        )
    }
}

@Preview(name = "Text Selected")
@Composable
fun FilterButton_TextSelected_Preview() {
    MaterialTheme {
        FilterButton(
            onClick = {},
            text = "Filter",
            selected = true,
            contentDescription = "text"
        )
    }
}

@Preview(name = "Icon + Text")
@Composable
fun FilterButton_IconText_Preview() {
    MaterialTheme {
        FilterButton(
            onClick = {},
            painter = painterResource(Res.drawable.ic_sort),
            text = "Filter",
            contentDescription = "text + count"
        )
    }
}

@Preview(name = "Icon + Text + Count")
@Composable
fun FilterButton_IconTextCount_Preview() {
    MaterialTheme {
        FilterButton(
            onClick = {},
            painter = painterResource(Res.drawable.ic_sort),
            text = "Filter",
            count = 2,
            contentDescription = "text + count"
        )
    }
}