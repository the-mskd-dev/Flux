package com.mskd.flux.features.search.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContainIgnoringCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow

class SearchViewModelTest : FunSpec({

    //region Setup

    fluxExtensions()

    lateinit var viewModel: SearchViewModel
    lateinit var database: DatabaseRepository
    lateinit var details: DetailsRepository
    lateinit var settingsDataStore: SettingsDataStore


    beforeTest {

        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
        }

        details = mockk(relaxed = true) {
            every { flowGenres() } returns MutableStateFlow(DetailsMockup.allGenres)
        }

        database = mockk(relaxed = true) {
            every { flowArtworks() } returns MutableStateFlow(MediaMockups.artworks.filter { !it.isUnknown })
        }

        viewModel = SearchViewModel(
            contentType = null,
            database = database,
            details = details,
            settingsDataStore = settingsDataStore
        )

    }

    //endregion

    //region Initial

    test("initial state") {

        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.actions.input shouldBe ""
            initialState.actions.selectedGenres shouldBe persistentListOf()
            initialState.actions.showGenresSelection shouldBe false
            initialState.artworks.shouldContainExactlyInAnyOrder(MediaMockups.artworks.filter { !it.isUnknown })

        }

    }

    test("initial state a contentType") {

        checkAll(
            Arb.enum<ContentType>()
        ) { type ->

            val customViewModel = SearchViewModel(
                contentType = type,
                database = database,
                details = details,
                settingsDataStore = settingsDataStore
            )
            customViewModel.uiState.test {
                val state = awaitItem()
                state.actions.selectedType shouldBe type
            }

        }

    }


    //endregion

    //region DoSearch

    test("DoSearch - user enters input") {

        checkAll(
            Arb.element(listOf("na", "nar", "no result"))
        ) { input ->

            // Given
            val expectedResult = MediaMockups.artworks.filter { !it.isUnknown && it.title.contains(input) }

            viewModel.uiState.test {

                awaitItem()

                // When
                viewModel.handleIntent(SearchIntent.DoSearch(input))

                // Then
                val state = awaitItem()
                state.actions.input shouldBe input
                state.artworks.size shouldBe expectedResult.size
                state.artworks.shouldContainExactlyInAnyOrder(expectedResult)

            }

        }

    }

    //endregion

    test("filter on movie type") {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.FilterOnType(contentType = ContentType.MOVIE))

            val state = awaitItem()

            state.actions.selectedType shouldBe ContentType.MOVIE
            state.artworks.all { it.type == ContentType.MOVIE } shouldBe true

        }

    }

    test("filter_on_show_type") {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.FilterOnType(contentType = ContentType.SHOW))

            val state = awaitItem()

            state.actions.selectedType shouldBe ContentType.SHOW
            state.artworks.all { it.type == ContentType.SHOW } shouldBe true

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

    test("filterOnType toggles back to null when same type is selected") {
        viewModel.uiState.test {
            awaitItem()

            // Filter on Movie first
            viewModel.handleIntent(SearchIntent.FilterOnType(contentType = ContentType.MOVIE))
            awaitItem().actions.selectedType shouldBe ContentType.MOVIE

            // Filter on Movie again
            viewModel.handleIntent(SearchIntent.FilterOnType(contentType = ContentType.MOVIE))
            awaitItem().actions.selectedType shouldBe null
        }
    }

})