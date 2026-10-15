package com.mskd.flux.features.people.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.people.domain.model.PersonDetails
import com.mskd.flux.features.people.domain.usecase.GetPersonDetailsUseCase
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PeopleMockup
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestDispatcher

class PersonViewModelTest : FunSpec({

    val testDispatcher: TestDispatcher = fluxExtensions()

    lateinit var viewModel: PersonViewModel
    lateinit var getPersonDetailsUseCase: GetPersonDetailsUseCase

    val person: Person = PeopleMockup.cast.first() // id = 82055, CAST

    val fullDetails = PersonDetails(
        id = person.id,
        name = "Junko Takeuchi",
        profilePath = "/zNjrblq3xS1idpCsmSl5P5eTon7.jpg",
        birthday = "1972-04-05",
        deathday = null,
        placeOfBirth = "Saitama, Japan",
        biography = "A Japanese voice actress.",
        knownForDepartment = "Acting",
        alsoKnownAs = listOf("Takeuchi Junko"),
        homepage = null,
        imdbId = "nm0847594",
        artworks = persistentListOf(MediaMockups.movieArtwork)
    )

    val offlineDetails = PersonDetails(
        id = person.id,
        name = person.name,
        profilePath = person.profilePath,
        artworks = persistentListOf(MediaMockups.movieArtwork)
    )

    val updateVm: () -> Unit = {

        viewModel = PersonViewModel(
            person = person,
            getPersonDetailsUseCase = getPersonDetailsUseCase
        )

    }

    beforeTest {

        getPersonDetailsUseCase = mockk()
        coEvery { getPersonDetailsUseCase(any()) } returns fullDetails

        updateVm()

    }

    test("initial state is loading until the details resolve") {

        // Given : the use case suspends until the gate is completed
        val gate = CompletableDeferred<PersonDetails>()
        coEvery { getPersonDetailsUseCase(any()) } coAnswers { gate.await() }
        updateVm()

        // When / Then
        viewModel.uiState.test {

            awaitItem().state shouldBe State.Loading

            gate.complete(fullDetails)
            testDispatcher.scheduler.advanceUntilIdle()

            val content = (awaitItem().state as State.Content).content
            content.details shouldBe fullDetails

        }

    }

    test("exposes the full details in content") {

        viewModel.uiState.test {

            val state = awaitItem()
            state.state.shouldBeInstanceOf<State.Content<PeopleContent>>()

            val content = (state.state as State.Content<PeopleContent>).content
            content.details shouldBe fullDetails

        }

    }

    test("offline fallback keeps content with the local data only") {

        // Given : the use case fell back to the basic People data
        coEvery { getPersonDetailsUseCase(any()) } returns offlineDetails
        updateVm()

        // When / Then : the screen stays in content, no error state
        viewModel.uiState.test {

            val state = expectMostRecentItem()
            state.state.shouldBeInstanceOf<State.Content<PeopleContent>>()

            val content = (state.state as State.Content<PeopleContent>).content
            content.details.biography shouldBe null
            content.details.birthday shouldBe null
            content.details.name shouldBe person.name
            content.details.artworks shouldBe persistentListOf(MediaMockups.movieArtwork)

        }

    }

    test("error state when the use case fails unexpectedly") {

        // Given
        coEvery { getPersonDetailsUseCase(any()) } throws RuntimeException("boom")
        updateVm()

        // When / Then
        viewModel.uiState.test {
            val state = awaitItem()
            state.state shouldBe State.Error()
        }

    }

    test("on back tap") {
        viewModel.event.test {
            viewModel.handleIntent(PeopleIntent.OnBackTap)
            awaitItem() shouldBe PeopleEvent.BackToPreviousScreen
        }
    }

    test("on artwork tap emits NavigateToMovie for a movie") {

        // Given
        val artwork = MediaMockups.movieArtwork // ContentType.MOVIE

        // When / Then
        viewModel.event.test {
            viewModel.handleIntent(PeopleIntent.OnArtworkClick(artwork = artwork, rgb = 12345))
            val event = awaitItem()
            event.shouldBeInstanceOf<PeopleEvent.NavigateToMovie>()
            event.artworkId shouldBe artwork.id
            event.rgb shouldBe 12345
        }

    }

    test("on artwork tap emits NavigateToShow for a show") {

        // Given
        val artwork = MediaMockups.showArtwork // ContentType.SHOW

        // When / Then
        viewModel.event.test {
            viewModel.handleIntent(PeopleIntent.OnArtworkClick(artwork = artwork, rgb = null))
            val event = awaitItem()
            event.shouldBeInstanceOf<PeopleEvent.NavigateToShow>()
            event.artworkId shouldBe artwork.id
            event.rgb shouldBe null
        }

    }

})
