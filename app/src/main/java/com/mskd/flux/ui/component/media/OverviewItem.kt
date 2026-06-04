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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.ui.component.global.ReadMoreButton
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui

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
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxWidth()
            .padding(all = Ui.Space.MEDIUM)
            .then(
                if (hasLaidOut) Modifier.animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                ) else Modifier
            ),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
        ) {

            topDetails()

            Text.Title.Large(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                emphasized = true
            )

            subtitle()

        }

        Text.Body.Large(
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

    Row(horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)) {
        Text.Label.Medium(
            text = stringResource(id = R.string.season, episode.season).uppercase(),
            emphasized = true,
            color = MaterialTheme.colorScheme.primary,
        )
        Text.Label.Medium(
            text = stringResource(id = R.string.episode, episode.number).uppercase(),
            emphasized = true,
            color = MaterialTheme.colorScheme.secondary
        )
    }

}

@Preview
@Composable
fun OverviewItem_Preview_Movie() {
    AppTheme {
        OverviewItem(
            title = stringResource(R.string.summary),
            description = MediaMockups.movie.description,
            subtitle = { MediaDetailsHorizontal(media = MediaMockups.movie, showRating = true) }
        )
    }
}

@Preview
@Composable
fun OverviewItem_Preview_Season() {
    AppTheme {
        OverviewItem(
            title = stringResource(R.string.summary),
            description = MediaMockups.season1.description,
        )
    }
}

@Preview
@Composable
fun OverviewItem_Preview_Episode() {
    AppTheme {
        OverviewItem(
            title = MediaMockups.episode1.title,
            description = MediaMockups.episode1.description,
            topDetails = { EpisodesDetails(episode = MediaMockups.episode1) },
            subtitle = { MediaDetailsHorizontal(media = MediaMockups.episode1, showRating = true) }
        )
    }
}

