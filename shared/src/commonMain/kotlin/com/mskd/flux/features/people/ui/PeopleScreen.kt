package com.mskd.flux.features.people.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.core.model.artwork.Artwork
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
import com.mskd.flux.ui.bleedHorizontal
import com.mskd.flux.ui.components.ErrorScreen
import com.mskd.flux.ui.components.FluxScaffold
import com.mskd.flux.ui.components.LoadingScreen
import com.mskd.flux.ui.dimensions.itemWidthFor
import com.mskd.flux.ui.dimensions.rememberScreenDimensions
import com.mskd.flux.ui.image.FluxImage
import com.mskd.flux.ui.image.rememberIconPlaceholderPainter
import com.mskd.flux.ui.media.MediaItem
import com.mskd.flux.ui.media.OverviewItem
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.formattedText
import com.mskd.flux.utils.extensions.parseTMDBDate
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.biography
import flux.shared.generated.resources.born
import flux.shared.generated.resources.died
import flux.shared.generated.resources.ic_person
import flux.shared.generated.resources.known_for
import flux.shared.generated.resources.known_for_department
import flux.shared.generated.resources.oups_an_error_occured
import flux.shared.generated.resources.place_of_birth
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PeopleScreen(
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
                PeopleScreenContent(
                    details = state.content.details,
                    sendIntent = { viewModel.handleIntent(it) }
                )
            }
        }

    }

}

@Composable
fun PeopleScreenContent(
    details: PersonDetails,
    sendIntent: (PeopleIntent) -> Unit
) {

    val isLargeScreen = rememberScreenDimensions().isLarge
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    FluxScaffold(
        title = details.name,
        animatedTitle = true,
        onBackTap = { sendIntent(PeopleIntent.OnBackTap) },
        scrollBehavior = scrollBehavior
    ) { innerPadding ->

        if (isLargeScreen) {
            PeopleContentLarge(
                details = details,
                scaffoldInnerPadding = innerPadding,
                sendIntent = sendIntent,
            )
        } else {
            PeopleContentRegular(
                details = details,
                scaffoldInnerPadding = innerPadding,
                sendIntent = sendIntent,
            )
        }

    }

}

@Composable
private fun ProfilePicture(
    modifier: Modifier = Modifier,
    details: PersonDetails
) {

    val placeholder = rememberIconPlaceholderPainter(icon = Res.drawable.ic_person)

    Surface(
        modifier = modifier,
        shape = FluxUI.shapes.corners,
        shadowElevation = FluxUI.Elevation.itemShadow
    ) {

        FluxImage(
            modifier = Modifier.fillMaxSize(),
            path = details.profilePath.orEmpty(),
            contentDescription = details.name,
            placeholder = placeholder,
            error = placeholder
        )

    }

}

@Composable
private fun PeopleHeader(
    modifier: Modifier = Modifier,
    details: PersonDetails
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
    ) {

        Text.Adaptive(
            modifier = Modifier.fillMaxWidth(),
            text = details.name,
            color = MaterialTheme.colorScheme.onBackground,
            style = Text.Style.mainTitle(),
            overflow = TextOverflow.Ellipsis
        )

        details.birthday?.parseTMDBDate()?.formattedText?.let { birthday ->
            Text.Content.Body(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.born, birthday),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        details.deathday?.parseTMDBDate()?.formattedText?.let { deathday ->
            Text.Content.Body(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.died, deathday),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        details.placeOfBirth?.let { placeOfBirth ->
            Text.Content.Body(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.place_of_birth, placeOfBirth),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        details.knownForDepartment?.let { department ->
            Text.Content.Body(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.known_for_department, department),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

    }

}

@Composable
private fun PeopleArtworks(
    artworks: List<Artwork>,
    sendIntent: (PeopleIntent) -> Unit
) {

    var itemWidth by remember { mutableStateOf(FluxUI.Dimension.itemWidth) }
    val density = LocalDensity.current

    Column(
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text.Content.Title(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.known_for),
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size ->
                    with(density) {
                        itemWidth = itemWidthFor(
                            screenWidthDp = size.width.toDp(),
                            columns = 3
                        )
                    }
                }
                .bleedHorizontal(),
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
            verticalAlignment = Alignment.Top,
            contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
        ) {

            items(items = artworks, key = { it.id }) { artwork ->

                MediaItem(
                    modifier = Modifier
                        .width(itemWidth)
                        .widthIn(max = FluxUI.Dimension.itemWidth),
                    path = artwork.imagePath,
                    description = artwork.title,
                    onClick = { rgb -> sendIntent(PeopleIntent.OnArtworkClick(artwork = artwork, rgb = rgb)) }
                )

            }

        }

    }

}

@Composable
fun PeopleContentRegular(
    details: PersonDetails,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (PeopleIntent) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FluxUI.Space.medium),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {

            Row(horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)) {

                ProfilePicture(
                    modifier = Modifier
                        .width(140.dp)
                        .aspectRatio(FluxUI.Dimension.itemRatio),
                    details = details
                )

                PeopleHeader(
                    modifier = Modifier.weight(1f),
                    details = details
                )

            }

        }

        if (!details.biography.isNullOrBlank()) {

            item {
                OverviewItem(
                    title = stringResource(Res.string.biography),
                    description = details.biography
                )
            }

        }

        if (details.artworks.isNotEmpty()) {

            item {
                PeopleArtworks(artworks = details.artworks, sendIntent = sendIntent)
            }

        }

        item {
            Spacer(modifier = Modifier
                .padding(bottom = FluxUI.Space.bottomScreen)
                .height(scaffoldInnerPadding.calculateBottomPadding())
            )
        }

    }

}

@Composable
fun PeopleContentLarge(
    details: PersonDetails,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (PeopleIntent) -> Unit
) {

    Row(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier.weight(.5f),
            contentAlignment = Alignment.Center
        ) {

            ProfilePicture(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(FluxUI.Dimension.itemRatio),
                details = details
            )

        }

        LazyColumn(
            modifier = Modifier.weight(.5f),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
        ) {

            item {
                Spacer(modifier = Modifier.height(scaffoldInnerPadding.calculateTopPadding()))
            }

            item {
                PeopleHeader(
                    modifier = Modifier.fillMaxWidth(),
                    details = details
                )
            }

            if (!details.biography.isNullOrBlank()) {

                item {
                    OverviewItem(
                        title = stringResource(Res.string.biography),
                        description = details.biography
                    )
                }

            }

            if (details.artworks.isNotEmpty()) {

                item {
                    PeopleArtworks(artworks = details.artworks, sendIntent = sendIntent)
                }

            }

            item {
                Spacer(modifier = Modifier
                    .padding(bottom = FluxUI.Space.bottomScreen)
                    .height(scaffoldInnerPadding.calculateBottomPadding())
                )
            }

        }

    }

}

@FluxPreview
@Composable
fun PeopleScreen_Preview() {
    FluxThemePreview {
        PeopleScreenContent(
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

