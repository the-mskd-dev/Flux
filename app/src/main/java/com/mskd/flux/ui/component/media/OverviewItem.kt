package com.mskd.flux.ui.component.media

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.component.global.ReadMoreButton
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.episode
import flux.shared.generated.resources.season
import flux.shared.generated.resources.summary
import org.jetbrains.compose.resources.stringResource

@Composable
fun OverviewItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    topDetails: @Composable () -> Unit = {},
    subtitle: @Composable () -> Unit = {}
) {

    var expanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }
    var hasLaidOut by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(FluxUI.shapes.corners)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxWidth()
            .padding(all = FluxUI.Space.medium)
            .then(
                if (hasLaidOut) Modifier.animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                ) else Modifier
            ),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
        ) {

            topDetails()

            Text.Content.Title(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
            )

            subtitle()

        }

        Text.Content.Body(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            text = description,
            textAlign = TextAlign.Left,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!expanded) {
                    isOverflowing = result.hasVisualOverflow
                    hasLaidOut = true
                }
            }
        )

        if (isOverflowing || expanded) {
            ReadMoreButton(
                modifier = Modifier.align(Alignment.End),
                onTap = { expanded = !expanded },
                isExpanded = expanded
            )
        }

    }

}

@Composable
fun EpisodesDetails(episode: Episode) {

    Row(horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)) {
        Text.Content.Label(
            text = stringResource(Res.string.season, episode.season).uppercase(),
            color = MaterialTheme.colorScheme.primary,
        )
        Text.Content.Label(
            text = stringResource(Res.string.episode, episode.number).uppercase(),
            color = MaterialTheme.colorScheme.secondary
        )
    }

}

@Preview
@Composable
fun OverviewItem_Preview_Movie() {
    FluxTheme {
        OverviewItem(
            title = stringResource(Res.string.summary),
            description = MediaMockups.movie.description,
            subtitle = { MediaDetailsHorizontal(media = MediaMockups.movie) }
        )
    }
}

@Preview
@Composable
fun OverviewItem_Preview_Season() {
    FluxTheme {
        OverviewItem(
            title = stringResource(Res.string.summary),
            description = MediaMockups.season1.description,
        )
    }
}

@Preview
@Composable
fun OverviewItem_Preview_Episode() {
    FluxTheme {
        OverviewItem(
            title = MediaMockups.episode1.title,
            description = MediaMockups.episode1.description,
            topDetails = { EpisodesDetails(episode = MediaMockups.episode1) },
            subtitle = { MediaDetailsHorizontal(media = MediaMockups.episode1) }
        )
    }
}

