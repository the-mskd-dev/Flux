package com.kaem.flux.screens.search

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.CatalogContent
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.artwork.ContentType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchViewModelTest : BaseTest() {

    private lateinit var viewModel: SearchViewModel
    private lateinit var catalogRepository: CatalogRepository

    // Mocked data
    private val libraryFlow = MutableStateFlow(
        CatalogContent(artworks = MediaMockups.artworks)
    )

    override fun setUp() {
        super.setUp()

        catalogRepository = mockk(relaxed = true) {
            every { catalogFlow } returns this@SearchViewModelTest.libraryFlow
        }

        viewModel = SearchViewModel(
            contentType = null,
            repository = catalogRepository
        )

    }

    @Test
    fun initial_state() = runTest {

        viewModel.uiState.test {

            val initialState = awaitItem()

            assert(initialState.searchWord == "")
            assert(initialState.artworks == MediaMockups.artworks)

        }

    }

    @Test
    fun search_word_with_one_result() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.DoSearch("nar"))

            val state = awaitItem()

            assert(state.searchWord == "nar")
            assert(state.filteredArtworks.size == 1)
            assert(state.filteredArtworks.any { it.title.contains("naruto", ignoreCase = true) })

        }

    }

    @Test
    fun search_word_with_multiple_results() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.DoSearch("na"))

            val state = awaitItem()

            assert(state.searchWord == "na")
            assert(state.filteredArtworks.size == 2)
            assert(state.filteredArtworks == MediaMockups.artworks)

        }

    }

    @Test
    fun search_word_with_no_result() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.DoSearch("spider-man"))

            val state = awaitItem()

            assert(state.searchWord == "spider-man")
            assert(state.filteredArtworks.isEmpty())

        }

    }

    @Test
    fun filter_on_movie_type() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.FilterOnType(contentType = ContentType.MOVIE))

            val state = awaitItem()

            assert(state.contentType == ContentType.MOVIE)
            assert(state.filteredArtworks.all { it.type == ContentType.MOVIE })

        }

    }

    @Test
    fun filter_on_show_type() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.FilterOnType(contentType = ContentType.SHOW))

            val state = awaitItem()

            assert(state.contentType == ContentType.SHOW)
            assert(state.filteredArtworks.all { it.type == ContentType.MOVIE })

        }

    }

}