package com.kaem.flux.screens.artwork

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kaem.flux.R
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Status
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.Placeholders
import com.kaem.flux.ui.component.Title
import com.kaem.flux.ui.theme.FluxElevation
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.utils.Constants
import com.kaem.flux.utils.inMinutes
import java.text.DateFormat

@Composable
fun ArtworkHeader(
    uiState: ArtworkUiState,
    onBackButtonTap: () -> Unit,
    onStatusButtonTap: () -> Unit,
    onPlayerButtonTap: () -> Unit
) {

    ConstraintLayout(
        modifier = Modifier.fillMaxWidth(),
        constraintSet = ArtworkHeaderConstraintSet
    ) {

        val imagePath = when (uiState.selectedArtwork) {
            is Episode -> uiState.selectedArtwork.imagePath
            else -> uiState.overview.bannerPath
        }

        ArtworkImage(
            modifier = Modifier
                .aspectRatio(6f / 5f)
                .layoutId("image"),
            imagePath = imagePath,
            title = uiState.overview.title
        )

        BackButton(
            modifier = Modifier.layoutId("back"),
            onTap = onBackButtonTap
        )

        ArtworkPlayerButton(
            modifier = Modifier.layoutId("play"),
            artwork = uiState.selectedArtwork,
            onTap = onPlayerButtonTap
        )

        ArtworkStatusButton(
            modifier = Modifier.layoutId("status"),
            onTap = { onStatusButtonTap() }
        )

        ArtworkTitle(
            modifier = Modifier.layoutId("title"),
            uiState = uiState
        )

    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArtworkImage(
    modifier: Modifier,
    imagePath: String,
    title: String
) {

    var imageHeight by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier.onSizeChanged { imageHeight = it.height },
        contentAlignment = Alignment.BottomCenter
    ) {

        GlideImage(
            modifier = Modifier.fillMaxSize(),
            model = Constants.TMDB.IMAGE + imagePath,
            contentScale = ContentScale.Crop,
            loading = Placeholders.loading,
            contentDescription = title
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = .3f),
                            MaterialTheme.colorScheme.background.copy(alpha = .6f),
                            MaterialTheme.colorScheme.background.copy(alpha = .9f),
                            MaterialTheme.colorScheme.background,
                        ),
                        startY = imageHeight * .0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

    }

}

@Composable
fun ArtworkPlayerButton(
    modifier: Modifier,
    artwork: Artwork?,
    onTap: () -> Unit
) {

    artwork ?: return

    val isWatching = artwork.status == Status.IS_WATCHING
    val text = if (isWatching) stringResource(R.string.resume) else stringResource(R.string.start)
    val buttonColor by animateColorAsState(
        targetValue = if (artwork.status == Status.WATCHED) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary,
        label = "playButtonColorAnimation"
    )
    val textColor by animateColorAsState(
        targetValue = if (artwork.status == Status.WATCHED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
        label = "playButtonTextColorAnimation"
    )

    Box(
        modifier = modifier.size(70.dp),
        contentAlignment = Alignment.Center
    ) {

        FloatingActionButton(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FluxElevation.floatingButtonElevation(),
            onClick = { onTap() },
            content = {
                Icon(
                    modifier = Modifier.size(45.dp),
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "play button"
                )
            }
        )

        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.onBackground,
            strokeWidth = 4.dp,
            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
            progress = { artwork.currentTime.inMinutes.toFloat() / artwork.duration.toFloat() }
        )

    }

}

@Composable
fun ArtworkStatusButton(
    modifier: Modifier,
    onTap: () -> Unit
) {

    FloatingActionButton(
        modifier = modifier.size(30.dp),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FluxElevation.floatingButtonElevation(),
        onClick = onTap,
        content = {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Rounded.Done, contentDescription = "check if watched button"
            )
        }
    )

}

@Composable
fun ArtworkTitle(
    modifier: Modifier,
    uiState: ArtworkUiState
) {

    Title(
        modifier = modifier,
        text = uiState.overview.title,
        textAlign = TextAlign.Start
    )

}

val ArtworkHeaderConstraintSet = ConstraintSet {

    val (image, back, title, play, status) = createRefsFor("image", "back", "title", "play", "status")
    constrain(image) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        width = Dimension.fillToConstraints
    }

    constrain(back) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
    }

    constrain(title) {
        bottom.linkTo(image.bottom, FluxSpace.SMALL)
        start.linkTo(parent.start, FluxSpace.MEDIUM)
        end.linkTo(play.start, FluxSpace.MEDIUM)
        width = Dimension.fillToConstraints
    }

    constrain(play) {
        top.linkTo(title.top)
        bottom.linkTo(title.bottom)
        end.linkTo(parent.end, FluxSpace.LARGE)
    }

    constrain(status) {
        top.linkTo(title.bottom, FluxSpace.LARGE)
        start.linkTo(title.start)
    }

}