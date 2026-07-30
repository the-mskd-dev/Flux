package com.mskd.flux.features.search.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContainIgnoringCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class SearchViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: SearchViewModel
    lateinit var database: DatabaseRepository
    lateinit var settingsDataStore: SettingsDataStore


    beforeTest {

        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
        }

        database = FakeDatabaseRepository()

        viewModel = SearchViewModel(
            contentType = null,
            database = database,
            settingsDataStore = settingsDataStore
        )

    }

    test("initial state") {

        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.searchWord shouldBe ""
            initialState.artworks shouldBe MediaMockups.artworks.filter { !it.isUnknown }

        }

    }

    test("search word with one result") {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.DoSearch("nar"))

            val state = awaitItem()

            state.searchWord shouldBe "nar"
            state.filteredArtworks.size shouldBe 1
            state.filteredArtworks.any { it.title.contains("naruto", ignoreCase = true) } shouldBe true

        }

    }

    test("search word with multiple results") {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.DoSearch("na"))

            val state = awaitItem()

            state.searchWord shouldBe "na"
            state.filteredArtworks.shouldForAll {
                it.title shouldContainIgnoringCase  "na"
            }
        }

    }

    test("search word with no result") {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.DoSearch("no result"))

            val state = awaitItem()

            state.searchWord shouldBe "no result"
            state.filteredArtworks.isEmpty() shouldBe true

        }

    }

    test("filter on movie type") {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.FilterOnType(contentType = ContentType.MOVIE))

            val state = awaitItem()

            state.contentType shouldBe ContentType.MOVIE
            state.filteredArtworks.all { it.type == ContentType.MOVIE } shouldBe true

        }

    }

    test("filter_on_show_type") {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.FilterOnType(contentType = ContentType.SHOW))

            val state = awaitItem()

            state.contentType shouldBe ContentType.SHOW
            state.filteredArtworks.all { it.type == ContentType.SHOW } shouldBe true

        }

    }

    test("on back tap") {
        viewModel.event.test {
            viewModel.handleIntent(SearchIntent.OnBackTap)
            awaitItem() shouldBe SearchEvent.BackToPreviousScreen
        }
    }

    test("on artwork show tap") {
        viewModel.event.test {
            viewModel.handleIntent(SearchIntent.OnArtworkTap(artwork = MediaMockups.showArtwork, rgb = 0xFFFFFF))
            awaitItem() shouldBe SearchEvent.NavigateToShow(artworkId = MediaMockups.showArtwork.id, rgb = 0xFFFFFF)
        }
    }

    test("on artwork movie tap") {
        viewModel.event.test {
            viewModel.handleIntent(SearchIntent.OnArtworkTap(artwork = MediaMockups.movieArtwork, rgb = 0xFFFFFF))
            awaitItem() shouldBe SearchEvent.NavigateToMovie(artworkId = MediaMockups.movieArtwork.id, rgb = 0xFFFFFF)
        }
    }

    test("initial state with non-null contentType") {
        val customViewModel = SearchViewModel(
            contentType = ContentType.MOVIE,
            database = database,
            settingsDataStore = settingsDataStore
        )
        customViewModel.uiState.test {
            val state = awaitItem()
            state.contentType shouldBe ContentType.MOVIE
            state.autoKeyboard shouldBe false
        }
    }

    test("filterOnType toggles back to null when same type is selected") {
        viewModel.uiState.test {
            awaitItem()

            // Filter on Movie first
            viewModel.handleIntent(SearchIntent.FilterOnType(contentType = ContentType.MOVIE))
            awaitItem().contentType shouldBe ContentType.MOVIE

            // Filter on Movie again
            viewModel.handleIntent(SearchIntent.FilterOnType(contentType = ContentType.MOVIE))
            awaitItem().contentType shouldBe null
        }
    }

})