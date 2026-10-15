package com.mskd.flux.features.people.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.people.domain.model.PersonDetails
import com.mskd.flux.features.people.presentation.PeopleEvent
import com.mskd.flux.features.people.presentation.PeopleIntent
import com.mskd.flux.features.people.presentation.PersonViewModel
import com.mskd.flux.mockups.PeopleMockup
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.components.ErrorScreen
import com.mskd.flux.ui.components.FluxScaffold
import com.mskd.flux.ui.components.LoadingScreen
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.oups_an_error_occured
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PersonScreen(
    person: Person,
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: PersonViewModel = koinViewModel(parameters = { parametersOf(person) })
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                PeopleEvent.BackToPreviousScreen -> onBack()
                is PeopleEvent.NavigateToMovie -> navigate(Route.Artwork(artworkId = event.artworkId, season = null, rgb = event.rgb))
                is PeopleEvent.NavigateToShow -> navigate(Route.Show(artworkId = event.artworkId, rgb = event.rgb))
            }
        }
    }

    AnimatedContent(
        targetState = uiState.state,
        label = "PeopleScreenState",
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { state ->
            when (state) {
                is State.Loading -> "loading"
                is State.Error -> "error"
                is State.Content -> "content_${state.content.details.id}"
            }
        }
    ) { state ->

        when (state) {
            State.Loading -> LoadingScreen()
            is State.Error -> {
                ErrorScreen(
                    message = stringResource(Res.string.oups_an_error_occured),
                    onBackButtonClick = { viewModel.handleIntent(PeopleIntent.OnBackTap) }
                )
            }
            is State.Content -> {
                PersonScreenContent(
                    details = state.content.details,
                    sendIntent = { viewModel.handleIntent(it) }
                )
            }
        }

    }

}



@FluxPreview
@Composable
fun PersonScreen_Preview() {
    FluxThemePreview {
        PersonScreenContent(
            details = PersonDetails(
                id = PeopleMockup.cast.first().id,
                name = PeopleMockup.cast.first().name,
                profilePath = PeopleMockup.cast.first().profilePath,
                birthday = "1972-04-05",
                placeOfBirth = "Saitama, Japan",
                biography = "Junko Takeuchi (born 5 April 1972) is a Japanese voice actress from Saitama Prefecture. She is affiliated with Office Osaka.",
                knownForDepartment = "Acting",
                artworks = MediaMockups.artworks.take(3).toImmutableList()
            ),
            sendIntent = {}
        )
    }
}

