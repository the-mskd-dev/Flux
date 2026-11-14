package com.mskd.flux.screens.movie

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
import com.mskd.flux.features.movie.presentation.MovieEvent
import com.mskd.flux.features.movie.presentation.MovieIntent
import com.mskd.flux.features.movie.presentation.MovieViewModel
import com.mskd.flux.features.movie.ui.MovieScreenContent
import com.mskd.flux.features.player.domain.model.PlayerParams
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
fun MovieScreen(
    artworkId: Long,
    colorScheme: ColorScheme,
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    onIdentified: (artworkId: Long, type: ContentType) -> Unit,
    viewModel: MovieViewModel = koinViewModel(parameters = { parametersOf(artworkId) })
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val launchExternalPlayer = rememberExternalPlayerAction(
        onProgressResult = { progress -> viewModel.handleIntent(MovieIntent.OnExternalPlayerResult(progress = progress)) },
        onFallbackToInternal = { media -> viewModel.handleIntent(MovieIntent.PlayMedia(media = media, forceInternal = true)) }
    )

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                MovieEvent.BackToPreviousScreen -> onBack()
                is MovieEvent.Identified -> onIdentified(event.artworkId, event.type)
                is MovieEvent.NavigateToPerson -> navigate(Route.Person(person = event.person))
                is MovieEvent.NavigateToGallery -> navigate(Route.Gallery(artworkId = event.artworkId, contentType = event.contentType))
                is MovieEvent.PlayMedia -> {
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
        label = "MovieScreenState",
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { state ->
            when (state) {
                is State.Loading -> "loading"
                is State.Error -> "error"
                is State.Content -> "content_${state.content.fullMovie.artwork.id}"
            }
        }
    ) { state ->

        when (state) {
            State.Loading -> LoadingScreen()
            is State.Error -> {
                ErrorScreen(
                    message = stringResource(Res.string.oups_an_error_occured),
                    onBackButtonClick = { viewModel.handleIntent(MovieIntent.OnBackTap) }
                )
            }
            is State.Content -> {
                MaterialTheme(colorScheme = colorScheme) {
                    MovieScreenContent(
                        content = state.content,
                        sendIntent = viewModel::handleIntent
                    )
                }
            }
        }

    }

}
