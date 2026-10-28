package com.mskd.flux.ui.media.episode

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.style.TextOverflow
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.ui.blurContent
import com.mskd.flux.ui.blurText
import com.mskd.flux.ui.components.ReadMoreButton
import com.mskd.flux.ui.grayScale
import com.mskd.flux.ui.media.MediaDetailsVertical
import com.mskd.flux.ui.media.MediaThumbnail
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI

@Composable
fun EpisodeItemSmall(
    modifier: Modifier = Modifier,
    episode: Episode,
    isSelected: Boolean,
    isExpanded: Boolean,
    isBlurred: Boolean = false,
    isSpoiler: Boolean = false,
    onClick: (Episode) -> Unit,
    onReadMoreTap: (Boolean) -> Unit,
    onToggleSpoiler: () -> Unit = {},
    dropDownMenu: @Composable ((isVisible: Boolean, onDismissRequest: () -> Unit) -> Unit)? = null
) {

    var isOverflowing by remember { mutableStateOf(false) }
    var hasLaidOut by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer)

    Column(
        modifier = modifier
            .clip(FluxUI.shapes.corners)
            .let { if (episode.isAvailable) it else it.grayScale() }
            .background(bgColor)
            .combinedClickable(
                onClick = { onClick(episode) },
                onLongClick = { showMenu = dropDownMenu != null }
            )
            .then(
                if (hasLaidOut) Modifier.animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                ) else Modifier
            )
            .fillMaxWidth()
            .padding(FluxUI.Space.medium),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
        ) {

            MediaThumbnail(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .blurContent(blurred = isBlurred)
                    .weight(.4f),
                media = episode,
            )

            Column(
                modifier = Modifier.weight(.6f),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall),
                horizontalAlignment = Alignment.Start
            ) {

                EpisodeTitle(
                    episode = episode,
                    isSelected = isSelected,
                    isBlurred = isBlurred
                )

                MediaDetailsVertical(media = episode)

            }

        }

        Text.Card.Body(
            modifier = Modifier
                .fillMaxWidth()
                .spoilerToggle(isSpoiler = isSpoiler, isBlurred = isBlurred, onToggle = onToggleSpoiler)
                .animateContentSize()
                .blurText(blurred = isBlurred),
            text = episode.description,
            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!isExpanded) {
                    isOverflowing = result.hasVisualOverflow
                    hasLaidOut = true
                }
            }
        )

        if (isOverflowing || isExpanded) {
            ReadMoreButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { onReadMoreTap(!isExpanded) },
                isExpanded = isExpanded
            )
        }

        dropDownMenu?.invoke(
            showMenu,
            { showMenu = false }
        )

    }

}