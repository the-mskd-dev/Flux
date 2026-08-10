package com.mskd.flux.features.search.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldMatchEach
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldContainIgnoringCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.orNull
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow

class SearchViewModelTest : FunSpec({

    //region Setup

    fluxExtensions()

    fun createViewModel(
        contentType: ContentType? = null,
        database: DatabaseRepository = mockk(relaxed = true) {
            every { flowArtworks() } returns MutableStateFlow(MediaMockups.artworks.filter { !it.isUnknown })
        },
        details: DetailsRepository = mockk(relaxed = true) {
            every { flowGenres() } returns MutableStateFlow(DetailsMockup.allGenres)
        },
        settings: SettingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
        }
    ) : SearchViewModel = SearchViewModel(
        contentType = contentType,
        database = database,
        details = details,
        settings = settings
    )

    //endregion

    //region Initial

    test("initial state") {

        val viewModel = createViewModel()

        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.actions.input shouldBe ""
            initialState.actions.selectedGenres shouldBe persistentListOf()
            initialState.actions.showGenresSelection shouldBe false
            initialState.artworks.shouldContainExactlyInAnyOrder(MediaMockups.artworks.filter { !it.isUnknown })

        }

    }

    test("initial state with a contentType") {

        checkAll(
            Arb.enum<ContentType>()
        ) { type ->

            val viewModel = createViewModel(contentType = type)

            viewModel.uiState.test {
                val state = awaitItem()
                state.actions.selectedType shouldBe type
            }

        }

    }


    //endregion

    //region Filter input

    test("DoSearch - user enters input") {

        checkAll(
            Arb.element(listOf("na", "nar", "no result"))
        ) { input ->

            // Given
            val viewModel = createViewModel()

            viewModel.uiState.test {

                awaitItem()

                // When
                viewModel.handleIntent(SearchIntent.DoSearch(input))

                // Then
                val state = awaitItem()
                state.actions.input shouldBe input
                state.artworks.shouldForAll { it.title shouldContainIgnoringCase input  }

            }

        }

    }

    //endregion

    //region Filter contentType

    test("FilterOnType - filter on type") {

        checkAll(
            Arb.enum<ContentType>()
        ) { type ->

            // Given
            val viewModel = createViewModel()

            viewModel.uiState.test {

                awaitItem()

                // When
                viewModel.handleIntent(SearchIntent.FilterOnType(contentType = type))

                // Then
                val state = awaitItem()
                state.actions.selectedType shouldBe type
                state.artworks.shouldForAll { it.type shouldBe type }
            }

        }

    }

    test("FilterOnType - remove type filter when same type is selected") {

        checkAll(
            Arb.enum<ContentType>()
        ) { type ->

            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem()

                // Filter on type
                viewModel.handleIntent(SearchIntent.FilterOnType(contentType = type))
                awaitItem().actions.selectedType shouldBe type

                // Filter on the same type again
                viewModel.handleIntent(SearchIntent.FilterOnType(contentType = type))
                awaitItem().actions.selectedType shouldBe null
            }

        }

    }

    //endregion

    //region Navigation

    test("OnBackTap - send BackToPreviousScreen event") {

        // Given
        val viewModel = createViewModel()

        viewModel.event.test {

            // When
            viewModel.handleIntent(SearchIntent.OnBackTap)

            // Then
            awaitItem() shouldBe SearchEvent.BackToPreviousScreen
        }

    }

    test("OnArtworkTap - Tap on a show, send NavigateToShow event") {

        checkAll(
            Arb.element(MediaMockups.artworks.filter { it.type == ContentType.SHOW }),
            Arb.int().orNull()
        ) { show, color ->

            // Given
            val viewModel = createViewModel()

            viewModel.event.test {

                // When
                viewModel.handleIntent(SearchIntent.OnArtworkTap(artwork = show, rgb = color))

                // Then
                awaitItem() shouldBe SearchEvent.NavigateToShow(artworkId = show.id, rgb = color)
            }

        }

    }

    test("OnArtworkTap - Tap on a movie, send NavigateToMovie event") {

        checkAll(
            Arb.element(MediaMockups.artworks.filter { it.type == ContentType.MOVIE }),
            Arb.int().orNull()
        ) { movie, color ->

            // Given
            val viewModel = createViewModel()

            viewModel.event.test {

                // When
                viewModel.handleIntent(SearchIntent.OnArtworkTap(artwork = movie, rgb = color))

                // Then
                awaitItem() shouldBe SearchEvent.NavigateToMovie(artworkId = movie.id, rgb = color)
            }

        }

    }

    //endregion

})