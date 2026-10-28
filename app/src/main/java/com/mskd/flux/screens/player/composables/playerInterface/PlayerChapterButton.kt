package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.domain.model.VideoChapterType
import com.mskd.flux.features.player.presentation.ChapterButton
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.next_episode
import flux.shared.generated.resources.skip
import flux.shared.generated.resources.skip_ending
import flux.shared.generated.resources.skip_opening
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlayerChapterButton(
    modifier: Modifier,
    chapterButton: ChapterButton?,
    bottomMargin: () -> Dp,
    sendIntent: (PlayerIntent) -> Unit
) {

    val animatedBottomMargin by animateDpAsState(
        targetValue = bottomMargin(),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "NextEpisodeButtonPosition"
    )

    val backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
    val size = ButtonDefaults.MediumContainerHeight
    val text = when (chapterButton) {
        is ChapterButton.NextEpisode -> stringResource(Res.string.next_episode)
        is ChapterButton.Skip -> when (chapterButton.chapter.type) {
            VideoChapterType.OPENING -> stringResource(Res.string.skip_opening)
            VideoChapterType.ENDING -> stringResource(Res.string.skip_ending)
            VideoChapterType.CHAPTER -> stringResource(Res.string.skip)
        }
        null -> ""
    }

    AnimatedVisibility(
        modifier = modifier.padding(bottom = animatedBottomMargin),
        visible = chapterButton != null,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {

        Button(
            modifier = Modifier.heightIn(size),
            onClick = { sendIntent(PlayerIntent.OnChapterButtonTap(button = chapterButton)) },
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = contentColorFor(backgroundColor)
            ),
            contentPadding = ButtonDefaults.contentPaddingFor(size),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 6.dp,
                hoveredElevation = 4.dp,
                focusedElevation = 6.dp
            ),
            content = {

                AnimatedContent(
                    targetState = text
                ) { t ->
                    Text.Button.Default(
                        text = t,
                        color = contentColorFor(backgroundColor),
                    )
                }

            }
        )

    }

}

@FluxPreview
@Composable
fun PlayerChapterButton_Preview() {
    FluxTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            PlayerChapterButton(
                modifier = Modifier,
                chapterButton = ChapterButton.Skip(chapter = VideoChapter(0, 0L, 0L, type = VideoChapterType.OPENING)),
                bottomMargin = { 0.dp },
                sendIntent = {}
            )
        }
    }
}