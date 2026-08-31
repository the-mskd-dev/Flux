package com.mskd.flux.features.search.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.utils.extensions.filterFor
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAnyOf
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContainIgnoringCase
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.subsequence
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum
import io.kotest.property.exhaustive.of
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow

class SearchViewModelTest : FunSpec({

    //region Setup

    fluxExtensions()

    fun createViewModel(
        withType: ContentType? = null,
        withGenre: Genre? = null,
        database: DatabaseRepository = mockk(relaxed = true) {
            every { flowArtworks() } returns MutableStateFlow(MediaMockups.artworks.filter { !it.isUnknown })
        },
        details: DetailsRepository = mockk(relaxed = true) {
            every { flowGenres() } returns MutableStateFlow(DetailsMockup.allGenres)
        },
        settings: SettingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
        },
    ) : SearchViewModel = SearchViewModel(
        withType = withType,
        withGenre = withGenre,
        database = database,
        details = details,
        settings = settings,
    )

    //endregion

    //region Initial

    test("Initial state") {

        checkAll(
            iterations = 30,
            Arb.subsequence(MediaMockups.artworks.filter { !it.isUnknown }),
            Arb.enum<ContentType>().orNull(),
            Arb.element(DetailsMockup.allGenres).orNull(),
            Arb.boolean()
        ) { artworks, type, genre, showKeyboard ->

            // Given
            val viewModel = createViewModel(
                withType = type,
                withGenre = genre,
                database = mockk(relaxed = true) {
                    every { flowArtworks() } returns MutableStateFlow(artworks)
                },
                details = mockk(relaxed = true) {
                    every { flowGenres() } returns MutableStateFlow(DetailsMockup.allGenres)
                },
                settings = mockk(relaxed = true) {
                    every { flow } returns MutableStateFlow(SettingsDataStore.State(autoKeyboard = showKeyboard))
                }
            )
            val expectedArtworks = artworks
                .filter { !it.isUnknown }
                .filter { if (type != null) it.type == type else true }
                .filter { if (genre != null) it.genreIds.contains(genre.id) else true }
            val expectedGenres = DetailsMockup.allGenres.filterFor(artworks)

            viewModel.uiState.test {

                // When
                val initialState = awaitItem()

                // Then
                initialState.artworks.shouldContainExactlyInAnyOrder(expectedArtworks)
                initialState.availableGenres.shouldContainExactlyInAnyOrder(expectedGenres)
                initialState.autoKeyboard shouldBe showKeyboard
                initialState.actions.selectedType shouldBe type
                if (genre != null)
                    initialState.actions.selectedGenres.shouldContain(genre.id)
                else
                    initialState.actions.selectedGenres.shouldBeEmpty()

                cancelAndIgnoreRemainingEvents()
            }

        }

    }

    test("initial state with a contentType") {

        checkAll(
            Exhaustive.enum<ContentType>()
        ) { type ->

            val viewModel = createViewModel(withType = type)

            viewModel.uiState.test {
                val state = awaitItem()
                state.actions.selectedType shouldBe type
                cancelAndIgnoreRemainingEvents()
            }

        }

    }

    test("initial state with a genre") {

        checkAll(
            iterations = 30,
            Arb.element(DetailsMockup.allGenres)
        ) { genre ->

            val viewModel = createViewModel(withGenre = genre)

            viewModel.uiState.test {
                val state = awaitItem()
                state.actions.selectedGenres shouldContainExactly persistentListOf(genre.id)
                cancelAndIgnoreRemainingEvents()
            }

        }

    }

    //endregion

    //region Filter input

    test("DoSearch - user enters input") {

        checkAll(
            Exhaustive.of("na", "nar", "no result")
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
                cancelAndIgnoreRemainingEvents()

            }

        }

    }

    //endregion

    //region Filter contentType

    test("FilterOnType - filter on type") {

        checkAll(
            Exhaustive.enum<ContentType>()
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
                cancelAndIgnoreRemainingEvents()
            }

        }

    }

    test("FilterOnType - remove type filter when same type is selected") {

        checkAll(
            Exhaustive.enum<ContentType>()
        ) { type ->

            // Given
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SearchIntent.FilterOnType(contentType = type)) // First selection
                awaitItem().actions.selectedType shouldBe type
                viewModel.handleIntent(SearchIntent.FilterOnType(contentType = type)) // Second selection

                // Then
                awaitItem().actions.selectedType shouldBe null
                cancelAndIgnoreRemainingEvents()

            }

        }

    }

    //endregion

    //region Filter genres

    test("ShowGenresSelection - show genres bottom sheet") {

        // Given
        val viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem()

            // When
            viewModel.handleIntent(SearchIntent.ShowGenresSelection(show = true))

            // Then
            awaitItem().actions.showGenresSelection shouldBe true
        }

    }

    test("SelectGenre - One - returns only matching artworks") {

        // Given
        val artworks = MediaMockups.artworks.filter { !it.isUnknown }
        val availableGenres = DetailsMockup.allGenres.filterFor(artworks)

        checkAll(
            iterations = 30,
            Arb.element(availableGenres)
        ){ genre ->

            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SearchIntent.SelectGenre(genre = genre))

                // Then
                val state = awaitItem()
                state.actions.selectedGenres shouldContain genre.id
                state.artworks shouldForAll { it.genreIds shouldContain genre.id }
                cancelAndIgnoreRemainingEvents()

            }

        }

    }

    test("SelectGenre - Multiple - returns only matching artworks") {

        // Given
        val artworks = MediaMockups.artworks.filter { !it.isUnknown }
        val availableGenres = DetailsMockup.allGenres.filterFor(artworks)

        checkAll(
            iterations = 50,
            Arb.subsequence(availableGenres).filter { it.isNotEmpty() }
        ){ genres ->

            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()

                // When
                genres.forEach { viewModel.handleIntent(SearchIntent.SelectGenre(genre = it)) }

                // Then
                val state = expectMostRecentItem()
                state.actions.selectedGenres shouldContainExactlyInAnyOrder genres.map { it.id }
                state.artworks shouldForAll { a -> a.genreIds shouldContainAnyOf genres.map { it.id } }
                cancelAndIgnoreRemainingEvents()

            }

        }

    }

    test("ClearGenres - clear genres selection") {

        // Given
        val artworks = MediaMockups.artworks.filter { !it.isUnknown }
        val availableGenres = DetailsMockup.allGenres.filter { genre -> artworks.any { it.genreIds.contains(genre.id) } }

        checkAll(
            iterations = 50,
            Arb.subsequence(availableGenres).filter { it.isNotEmpty() }
        ){ genres ->

            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()

                // When
                genres.forEach { viewModel.handleIntent(SearchIntent.SelectGenre(genre = it)) }
                viewModel.handleIntent(SearchIntent.ClearGenres)

                // Then
                val state = expectMostRecentItem()
                state.actions.selectedGenres shouldBe persistentListOf()
                state.artworks shouldContainExactlyInAnyOrder artworks
                cancelAndIgnoreRemainingEvents()

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
            iterations = 30,
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
                cancelAndIgnoreRemainingEvents()
            }

        }

    }

    test("OnArtworkTap - Tap on a movie, send NavigateToMovie event") {

        checkAll(
            iterations = 30,
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
                cancelAndIgnoreRemainingEvents()
            }

        }

    }

    //endregion

})