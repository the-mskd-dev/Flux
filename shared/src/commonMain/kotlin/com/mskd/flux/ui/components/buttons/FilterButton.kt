package com.mskd.flux.ui.components.buttons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.expressive.CONTENT_SIZE_DAMPING
import com.mskd.flux.ui.expressive.CONTENT_SIZE_STIFFNESS
import com.mskd.flux.ui.expressive.expressiveContentSize
import com.mskd.flux.ui.expressive.expressivePress
import com.mskd.flux.ui.text.Text
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_sort
import flux.shared.generated.resources.sort
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val FilterItemEnter = fadeIn() + scaleIn(
    initialScale = 0.6f,
    animationSpec = spring(
        dampingRatio = CONTENT_SIZE_DAMPING,
        stiffness = CONTENT_SIZE_STIFFNESS
    )
)

@Composable
fun FilterButton(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    text: String? = null,
    count: Int = 0,
    onClick: () -> Unit,
    selected: Boolean = false,
    contentDescription: String
) {

    val containerColor by animateColorAsState(
        if (selected || count > 0) MaterialTheme.colorScheme.tertiary
        else MaterialTheme.colorScheme.tertiaryContainer
    )

    val contentColor by animateColorAsState(
        if (selected || count > 0) MaterialTheme.colorScheme.onTertiary
        else MaterialTheme.colorScheme.onTertiaryContainer
    )

    FilledTonalButton(
        modifier = modifier
            .expressivePress(),
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {

        var displayedCount by remember { mutableIntStateOf(count) }
        if (count > 0) displayedCount = count

        Row(
            modifier = Modifier.expressiveContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Design.Space.extraSmall)
        ) {

            AnimatedVisibility(
                visible = selected,
                enter = FilterItemEnter,
                exit = ExitTransition.None
            ) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    tint = contentColor,
                    contentDescription = "$contentDescription selected",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }

            painter?.let {
                Icon(
                    painter = it,
                    contentDescription = contentDescription
                )
            }

            text?.let {
                Text.Button.Chip(
                    text = it
                )
            }

            AnimatedVisibility(
                visible = count > 0,
                enter = FilterItemEnter,
                exit = ExitTransition.None
            ) {
                Text.Button.Chip(
                    text = "($displayedCount)"
                )
            }

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