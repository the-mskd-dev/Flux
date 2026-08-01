package com.mskd.flux.screens.sources.composables.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.downloads
import flux.shared.generated.resources.movies
import org.jetbrains.compose.resources.stringResource

@Composable
fun SystemSourceItem(name: String) {

    ListItem(
        modifier = Modifier.fillMaxWidth(),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        headlineContent = {
            Text.List.Title(
                text = name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
    )

}

@Preview
@Composable
fun SystemSourceItem_Preview() {
    FluxThemePreview {
        Column(
            modifier = Modifier
                .padding(FluxUI.Space.medium)
                .clip(FluxUI.shapes.corners),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
        ) {
            SystemSourceItem(name = stringResource(Res.string.movies))
            SystemSourceItem(name = stringResource(Res.string.downloads))
        }
    }
}