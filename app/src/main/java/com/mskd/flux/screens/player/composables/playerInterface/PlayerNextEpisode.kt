package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.screens.player.PlayerIntent
import com.mskd.flux.screens.player.PlayerUiContent
import com.mskd.flux.ui.component.global.CountDownButton
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerNextEpisode(
    modifier: Modifier,
    nextButton: PlayerUiContent.NextButton,
    bottomMargin: () -> Dp,
    sendIntent: (PlayerIntent) -> Unit
) {

    val episode = (nextButton as? PlayerUiContent.NextButton.Showed)?.episode

    val animatedBottomMargin by animateDpAsState(
        targetValue = bottomMargin(),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "NextEpisodeButtonPosition"
    )

    AnimatedVisibility(
        modifier = modifier
            .padding(bottom = animatedBottomMargin)
            .clickable { episode?.let { sendIntent(PlayerIntent.PlayNextEpisode(it)) } },
        visible = nextButton is PlayerUiContent.NextButton.Showed,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.medium)
        ) {

            FloatingActionButton(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .animateEnterExit(
                        enter = slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + scaleIn(),
                        exit = slideOutHorizontally { fullWidth -> fullWidth } + scaleOut()
                    ),
                onClick = { sendIntent(PlayerIntent.CancelNextEpisode) },
                shape = FloatingActionButtonDefaults.mediumShape,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Default.Close,
                    contentDescription = "play button"
                )
            }

            CountDownButton(
                onTap = { episode?.let { sendIntent(PlayerIntent.PlayNextEpisode(it)) } },
                text = { stringResource(R.string.next_episode, it) }
            )

        }

    }

}

@FluxPreview
@Composable
fun PlayerNextEpisode_Preview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            PlayerNextEpisode(
                modifier = Modifier,
                nextButton = PlayerUiContent.NextButton.Showed(MediaMockups.episode1),
                bottomMargin = { 0.dp },
                sendIntent = {}
            )
        }
    }
}