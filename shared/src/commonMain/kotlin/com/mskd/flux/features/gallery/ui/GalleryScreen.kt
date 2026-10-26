package com.mskd.flux.features.gallery.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.gallery.presentation.GalleryEvent
import com.mskd.flux.features.gallery.presentation.GalleryFailure
import com.mskd.flux.features.gallery.presentation.GalleryIntent
import com.mskd.flux.features.gallery.presentation.GalleryViewModel
import com.mskd.flux.ui.components.ErrorScreen
import com.mskd.flux.ui.components.LoadingScreen
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.oups_an_error_occured
import flux.shared.generated.resources.unavailable_check_connection
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun GalleryScreen(
    artworkId: Long,
    contentType: ContentType,
    onBack: () -> Unit,
    viewModel: GalleryViewModel = koinViewModel(parameters = { parametersOf(artworkId, contentType) })
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                GalleryEvent.BackToPreviousScreen -> onBack()
            }
        }
    }

    AnimatedContent(
        targetState = uiState.state,
        label = "GalleryScreenState",
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { state ->
            when (state) {
                is State.Loading -> "loading"
                is State.Error -> "error"
                is State.Content -> "content"
            }
        }
    ) { state ->

        when (state) {
            State.Loading -> LoadingScreen()
            is State.Error -> {
                ErrorScreen(
                    message = stringResource(
                        if (uiState.failure == GalleryFailure.OFFLINE) Res.string.unavailable_check_connection
                        else Res.string.oups_an_error_occured
                    ),
                    onRetryClick = { viewModel.handleIntent(GalleryIntent.OnRetryTap) },
                    onBackButtonClick = { viewModel.handleIntent(GalleryIntent.OnBackTap) }
                )
            }
            is State.Content -> {
                GalleryScreenContent(
                    title = uiState.title,
                    images = state.content.images,
                    failure = uiState.failure,
                    sendIntent = { viewModel.handleIntent(it) }
                )
            }
        }

    }

}
