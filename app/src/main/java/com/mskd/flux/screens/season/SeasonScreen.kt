package com.mskd.flux.screens.season

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.player.domain.model.PlayerParams
import com.mskd.flux.features.season.presentation.SeasonEvent
import com.mskd.flux.features.season.presentation.SeasonIntent
import com.mskd.flux.features.season.presentation.SeasonViewModel
import com.mskd.flux.features.season.ui.SeasonScreenContent
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.ui.components.ErrorScreen
import com.mskd.flux.ui.components.LoadingScreen
import com.mskd.flux.utils.rememberExternalPlayerAction
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.oups_an_error_occured
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SeasonScreen(
    artworkId: Long,
    season: Int,
    colorScheme: ColorScheme,
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    onIdentified: (artworkId: Long, type: ContentType) -> Unit,
    viewModel: SeasonViewModel = koinViewModel(parameters = { parametersOf(artworkId, season) })
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val launchExternalPlayer = rememberExternalPlayerAction(
        onProgressResult = { progress -> viewModel.handleIntent(SeasonIntent.OnExternalPlayerResult(progress = progress)) },
        onFallbackToInternal = { media -> viewModel.handleIntent(SeasonIntent.PlayMedia(media = media, forceInternal = true)) }
    )

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SeasonEvent.BackToPreviousScreen -> onBack()
                is SeasonEvent.Identified -> onIdentified(event.artworkId, event.type)
                is SeasonEvent.NavigateToGallery -> navigate(Route.Gallery(artworkId = event.artworkId, contentType = event.contentType))
                is SeasonEvent.PlayMedia -> {
                    if (event.externalPlayer)
                        launchExternalPlayer(event.media)
                    else
                        navigate(Route.Player(params = PlayerParams.fromMedia(event.media)))
                }
            }
        }
    }

    AnimatedContent(
        targetState = uiState.state,
        label = "SeasonScreenState",
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { state ->
            when (state) {
                is State.Loading -> "loading"
                is State.Error -> "error"
                is State.Content -> "content_${state.content.fullShow.artwork.id}"
            }
        }
    ) { state ->

        when (state) {
            State.Loading -> LoadingScreen()
            is State.Error -> {
                ErrorScreen(
                    message = stringResource(Res.string.oups_an_error_occured),
                    onBackButtonClick = { viewModel.handleIntent(SeasonIntent.OnBackTap) }
                )
            }
            is State.Content -> {
                MaterialTheme(colorScheme = colorScheme) {
                    SeasonScreenContent(
                        content = state.content,
                        sendIntent = viewModel::handleIntent
                    )
                }
            }
        }

    }

}
