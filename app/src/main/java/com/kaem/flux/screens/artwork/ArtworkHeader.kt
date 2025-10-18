package com.kaem.flux.screens.artwork

import android.graphics.drawable.Icon
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.atMost
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kaem.flux.R
import com.kaem.flux.mockups.ArtworkMockups
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Status
import com.kaem.flux.ui.component.ProgressBar
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.FluxTextButton
import com.kaem.flux.ui.component.Placeholders
import com.kaem.flux.ui.component.SmallText
import com.kaem.flux.ui.component.Title
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants
import com.kaem.flux.utils.extensions.minToMs
import com.kaem.flux.utils.extensions.timeDescription

@Composable
fun ArtworkHeader(
    overview: ArtworkOverview,
    artwork: Artwork?,
    zoom: Float,
    onBackButtonTap: () -> Unit,
    onStatusButtonTap: () -> Unit,
    onPlayerButtonTap: () -> Unit
) {

    ConstraintLayout(
        modifier = Modifier.fillMaxWidth(),
        constraintSet = ArtworkHeaderConstraintSet
    ) {

        val imagePath = when (artwork) {
            is Episode -> artwork.imagePath
            else -> overview.bannerPath
        }

        ArtworkImage(
            modifier = Modifier
                .aspectRatio(6f / 5f)
                .layoutId("image"),
            zoom = zoom,
            imagePath = imagePath,
            title = overview.title
        )

        BackButton(
            modifier = Modifier.layoutId("back"),
            onTap = onBackButtonTap
        )

        ArtworkPlayerButton(
            modifier = Modifier.layoutId("play"),
            artwork = artwork,
            onTap = onPlayerButtonTap
        )

        ArtworkStatusButton(
            modifier = Modifier.layoutId("status"),
            artwork = artwork,
            onTap = { onStatusButtonTap() }
        )

        ArtworkTitle(
            modifier = Modifier.layoutId("title"),
            title = overview.title
        )

    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArtworkImage(
    modifier: Modifier,
    zoom: Float,
    imagePath: String,
    title: String
) {

    var imageHeight by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier.onSizeChanged { imageHeight = it.height },
        contentAlignment = Alignment.BottomCenter
    ) {

        GlideImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                    translationX = (size.width * (1 - zoom)) / 2
                    translationY = (size.height * (1 - zoom)) / 2
                },
            model = Constants.TMDB.IMAGE + imagePath,
            contentScale = ContentScale.Crop,
            loading = Placeholders.loading(),
            failure = Placeholders.failure(),
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

    val backgroundColor by animateColorAsState(
        targetValue = if (artwork.status == Status.WATCHED) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
        label = "ArtworkPlayerButton backgroundColor animation"
    )

    val textColor by animateColorAsState(
        targetValue = if (artwork.status == Status.WATCHED) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
        label = "ArtworkPlayerButton backgroundColor animation"
    )

    val text = when (artwork.status) {
        Status.WATCHED -> stringResource(R.string.rewatch)
        Status.IS_WATCHING -> stringResource(R.string.resume)
        else -> stringResource(R.string.start)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        ArtworkStatusProgression(artwork = artwork)

        FluxButton(
            modifier = Modifier.fillMaxWidth(),
            text = text.uppercase(),
            onTap = onTap,
            icon = if (artwork.status == Status.WATCHED) Icons.Default.Refresh else Icons.Default.PlayArrow,
            backgroundColor = backgroundColor,
            textColor = textColor
        )

    }

}

@Composable
fun ArtworkStatusProgression(artwork: Artwork) {

    AnimatedVisibility(
        visible = artwork.status == Status.IS_WATCHING,
        label = "ArtworkStatusProgression animation"
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
            verticalAlignment = Alignment.CenterVertically
        ){

            ProgressBar(
                modifier = Modifier.weight(1f),
                artwork = artwork
            )

            val remainingTime = (artwork.duration.minToMs - artwork.currentTime).timeDescription(withoutSeconds = true)
            SmallText(
                text = stringResource(R.string.remaining_time, remainingTime),
                color = MaterialTheme.colorScheme.onBackground
            )

        }

    }

}

@Composable
fun ArtworkStatusButton(
    modifier: Modifier,
    artwork: Artwork?,
    onTap: () -> Unit
) {

    artwork ?: return

    AnimatedContent(
        modifier = modifier,
        targetState = (if (artwork.status == Status.WATCHED) stringResource(R.string.mark_as_not_watched) else stringResource(R.string.mark_as_watched)).uppercase(),
        contentAlignment = Alignment.Center,
        label = "ArtworkStatusButton animation"
    ) { text ->
        FluxTextButton(
            text = text,
            onTap = onTap
        )
    }

}

@Composable
fun ArtworkTitle(
    modifier: Modifier,
    title: String,
) {

    Title(
        modifier = modifier,
        text = title,
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
        start.linkTo(parent.start, Ui.Space.MEDIUM)
    }

    constrain(title) {
        bottom.linkTo(image.bottom, Ui.Space.MEDIUM)
        start.linkTo(parent.start, Ui.Space.MEDIUM)
        end.linkTo(parent.end, Ui.Space.MEDIUM)
        width = Dimension.fillToConstraints
    }

    constrain(play) {
        top.linkTo(title.bottom, Ui.Space.LARGE.times(2))
        start.linkTo(parent.start, Ui.Space.MEDIUM)
        end.linkTo(parent.end, Ui.Space.MEDIUM)
        width = Dimension.fillToConstraints.atMost(300.dp)
    }

    constrain(status) {
        top.linkTo(play.bottom, Ui.Space.SMALL)
        start.linkTo(parent.start, Ui.Space.MEDIUM)
        end.linkTo(parent.end, Ui.Space.MEDIUM)
        width = Dimension.fillToConstraints.atMost(300.dp)
    }

}

@Preview(showBackground = true)
@Composable
fun ArtworkHeader_Preview() {
    ArtworkHeader(
        overview = ArtworkMockups.showOverview,
        artwork = ArtworkMockups.episode1.copy(status = Status.IS_WATCHING, currentTime = 123456L),
        zoom = 1f,
        onBackButtonTap = {},
        onStatusButtonTap = {},
    ) { }
}