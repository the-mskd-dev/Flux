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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.atMost
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
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
    sendIntent: (MediaIntent) -> Unit,
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        MediaImage(
            modifier = Modifier.aspectRatio(6f / 5f),
            imagePath = overview.imagePath,
            title = overview.title,
            sendIntent = sendIntent
        )

        MediaPlayerButton(
            modifier = Modifier
                .padding(top = Ui.Space.LARGE.times(2))
                .widthIn(max = 300.dp)
                .fillMaxWidth(),
            media = media,
            onTap = { sendIntent(MediaIntent.ShowPlayer) }
        )

        MediaStatusButton(
            modifier = Modifier
                .padding(top = Ui.Space.SMALL)
                .widthIn(max = 300.dp)
                .fillMaxWidth(),
            media = media,
            onTap = { sendIntent(MediaIntent.ChangeWatchStatus(checkPrevious = true)) }
        )

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaImage(
    modifier: Modifier,
    imagePath: String,
    title: String,
    sendIntent: (MediaIntent) -> Unit
) {

    var imageHeight by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.onSizeChanged { imageHeight = it.height },) {

        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .blur(radius = 15.dp),
            model = ImageRequest.Builder(LocalContext.current)
                .data(Constants.TMDB.IMAGE + imagePath)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .4f)),
            error = ColorPainter(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .4f)),
            alpha = .2f,
            contentDescription = "contentDescription"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = .6f),
                            MaterialTheme.colorScheme.background.copy(alpha = .9f),
                            MaterialTheme.colorScheme.background,
                        ),
                        startY = imageHeight * .7f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {  },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    BackButton(onTap = {sendIntent(MediaIntent.OnBackTap)})
                },
            )

            Image(
                modifier = Modifier
                    .clip(Ui.Shape.Corner.Small)
                    .width(160.dp)
                    .aspectRatio(2f/3f),
                url = Constants.TMDB.IMAGE + imagePath,
                contentDescription = title
            )

        }

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

@Preview(showBackground = true)
@Composable
fun MediaHeader_Preview() {
    FluxTheme {
        MediaHeader(
            overview = MediaMockups.showOverview,
            media = MediaMockups.episode1.copy(status = Status.IS_WATCHING, currentTime = 123456L),
            sendIntent = {},
        )
    }
}