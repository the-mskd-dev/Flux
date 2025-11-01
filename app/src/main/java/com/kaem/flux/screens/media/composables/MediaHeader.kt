package com.kaem.flux.screens.media.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Media
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.model.media.Status
import com.kaem.flux.screens.media.MediaIntent
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.FluxTextButton
import com.kaem.flux.ui.component.Image
import com.kaem.flux.ui.component.ProgressBar
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants
import com.kaem.flux.utils.extensions.minToMs
import com.kaem.flux.utils.extensions.timeDescription

@Composable
fun MediaHeader(
    overview: MediaOverview,
    media: Media?,
    zoom: Float,
    sendIntent: (MediaIntent) -> Unit,
) {

    ConstraintLayout(
        modifier = Modifier.fillMaxWidth(),
        constraintSet = MediaHeaderConstraintSet
    ) {

        val imagePath = when (media) {
            is Episode -> media.imagePath
            else -> overview.bannerPath
        }

        MediaImage(
            modifier = Modifier
                .aspectRatio(6f / 5f)
                .layoutId("image"),
            zoom = zoom,
            imagePath = imagePath,
            title = overview.title
        )

        BackButton(
            modifier = Modifier.layoutId("back"),
            onTap = { sendIntent(MediaIntent.OnBackTap) }
        )

        MediaPlayerButton(
            modifier = Modifier.layoutId("play"),
            media = media,
            onTap = { sendIntent(MediaIntent.ShowPlayer) }
        )

        MediaStatusButton(
            modifier = Modifier.layoutId("status"),
            media = media,
            onTap = { sendIntent(MediaIntent.ChangeWatchStatus(checkPrevious = true)) }
        )

        MediaTitle(
            modifier = Modifier.layoutId("title"),
            title = overview.title
        )

    }

}

@Composable
fun MediaImage(
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

        Image(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                    translationX = (size.width * (1 - zoom)) / 2
                    translationY = (size.height * (1 - zoom)) / 2
                },
            url = Constants.TMDB.IMAGE + imagePath,
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
fun MediaPlayerButton(
    modifier: Modifier,
    media: Media?,
    onTap: () -> Unit
) {

    media ?: return

    val backgroundColor by animateColorAsState(
        targetValue = if (media.status == Status.WATCHED) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
        label = "MediaPlayerButton backgroundColor animation"
    )

    val textColor by animateColorAsState(
        targetValue = if (media.status == Status.WATCHED) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
        label = "MediaPlayerButton backgroundColor animation"
    )

    val text = when (media.status) {
        Status.WATCHED -> stringResource(R.string.rewatch)
        Status.IS_WATCHING -> stringResource(R.string.resume)
        else -> stringResource(R.string.start)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        MediaStatusProgression(media = media)

        FluxButton(
            modifier = Modifier.fillMaxWidth(),
            text = text.uppercase(),
            onTap = onTap,
            icon = if (media.status == Status.WATCHED) Icons.Default.Refresh else Icons.Default.PlayArrow,
            backgroundColor = backgroundColor,
            textColor = textColor
        )

    }

}

@Composable
fun MediaStatusProgression(media: Media) {

    AnimatedVisibility(
        visible = media.status == Status.IS_WATCHING,
        label = "MediaStatusProgression animation"
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
            verticalAlignment = Alignment.CenterVertically
        ){

            ProgressBar(
                modifier = Modifier.weight(1f),
                media = media
            )

            val remainingTime = (media.duration.minToMs - media.currentTime).timeDescription(withoutSeconds = true)
            Text.Label.Medium(
                text = stringResource(R.string.remaining_time, remainingTime),
                color = MaterialTheme.colorScheme.onBackground
            )

        }

    }

}

@Composable
fun MediaStatusButton(
    modifier: Modifier,
    media: Media?,
    onTap: () -> Unit
) {

    media ?: return

    AnimatedContent(
        modifier = modifier,
        targetState = (if (media.status == Status.WATCHED) stringResource(R.string.mark_as_not_watched) else stringResource(R.string.mark_as_watched)).uppercase(),
        contentAlignment = Alignment.Center,
        label = "MediaStatusButton animation"
    ) { text ->
        FluxTextButton(
            text = text,
            onTap = onTap
        )
    }

}

@Composable
fun MediaTitle(
    modifier: Modifier,
    title: String,
) {

    Text.Title.Large(
        modifier = modifier,
        text = title,
        textAlign = TextAlign.Start,
        color = MaterialTheme.colorScheme.onBackground
    )

}

val MediaHeaderConstraintSet = ConstraintSet {

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
fun MediaHeader_Preview() {
    FluxTheme {
        MediaHeader(
            overview = MediaMockups.showOverview,
            media = MediaMockups.episode1.copy(status = Status.IS_WATCHING, currentTime = 123456L),
            zoom = 1f,
            sendIntent = {},
        )
    }
}