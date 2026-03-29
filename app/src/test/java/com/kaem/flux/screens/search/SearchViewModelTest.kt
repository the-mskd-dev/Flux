package com.kaem.flux.screens.search

import app.cash.turbine.test
import com.kaem.flux.configs.fluxExtensions
import com.kaem.flux.data.repository.catalog.CatalogRepository
import com.kaem.flux.mockups.FakeCatalogRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.artwork.ContentType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SearchViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: SearchViewModel
    lateinit var catalogRepository: FakeCatalogRepository


    beforeTest {

        catalogRepository = FakeCatalogRepository(
            initialState = CatalogRepository.State(
                isLoading = false,
                artworks = MediaMockups.artworks
            )
        )

        viewModel = SearchViewModel(
            contentType = null,
            repository = catalogRepository
        )

    }

    test("initial state") {

        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.searchWord shouldBe ""
            initialState.artworks shouldBe MediaMockups.artworks

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
            state.filteredArtworks.size shouldBe 2
            state.filteredArtworks shouldBe MediaMockups.artworks

        }

    }

    test("search word with no result") {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.DoSearch("spider-man"))

            val state = awaitItem()

            state.searchWord shouldBe "spider-man"
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

})