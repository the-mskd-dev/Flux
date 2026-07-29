package com.mskd.flux.screens.setup.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskd.flux.screens.sources.composables.sourcesAnnotatedString
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.setup_sources_default_desc
import flux.shared.generated.resources.setup_sources_default_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SetupSourcesItem(
    title: String,
    description: AnnotatedString,
    isSelected: Boolean,
    onTap: () -> Unit
) {

    val density = LocalDensity.current
    var boxSizeDp by remember { mutableStateOf(0.dp) }

    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) boxSizeDp / 2 else 8.dp,
        animationSpec = tween(300),
        label = "cornerRadius"
    )

    Column(
        modifier = Modifier
            .onSizeChanged { size ->
                boxSizeDp = with(density) {
                    minOf(size.width, size.height).toDp()
                }
            }
            .clip(RoundedCornerShape(cornerRadius))
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .clickable { onTap() }
            .padding(vertical = FluxUI.Space.medium, horizontal = FluxUI.Space.medium),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        Text.Title.Medium(
            text = title,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Text.Annotated(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

    }

}

@Preview
@Composable
fun SetupSourcesItem_Selected_Preview() {
    FluxThemePreview() {
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = FluxUI.Space.medium)
            ,
            contentAlignment = Alignment.Center
        ) {
            SetupSourcesItem(
                title = stringResource(Res.string.setup_sources_default_title),
                description = sourcesAnnotatedString(Res.string.setup_sources_default_desc),
                isSelected = true,
                onTap = {  }
            )
        }
    }
}

@Preview
@Composable
fun SetupSourcesItem_Unselected_Preview() {
    FluxThemePreview {
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = FluxUI.Space.medium)
            ,
            contentAlignment = Alignment.Center
        ) {
            SetupSourcesItem(
                title = stringResource(Res.string.setup_sources_default_title),
                description = sourcesAnnotatedString(Res.string.setup_sources_default_desc),
                isSelected = false,
                onTap = {  }
            )
        }
    }
}