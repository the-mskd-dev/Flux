package com.kaem.flux.screens.search

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.CatalogContent
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.mockups.MediaMockups
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
        CatalogContent(mediaOverviews = MediaMockups.overviews)
    )

    override fun setUp() {
        super.setUp()

        catalogRepository = mockk(relaxed = true) {
            every { catalogFlow } returns this@SearchViewModelTest.libraryFlow
        }

        viewModel = SearchViewModel(catalogRepository)

    }

    @Test
    fun `initial state`() = runTest {

        viewModel.uiState.test {

            val initialState = awaitItem()

            assert(initialState.searchWord == "")
            assert(initialState.overviews == MediaMockups.overviews)

        }

    }

    @Test
    fun `search word with one result`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.DoSearch("nar"))

            val state = awaitItem()

            assert(state.searchWord == "nar")
            assert(state.filteredOverviews.size == 1)
            assert(state.filteredOverviews.any { it.title.equals("naruto", ignoreCase = true) })

        }

    }

    @Test
    fun `search word with multiple results`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.DoSearch("na"))

            val state = awaitItem()

            assert(state.searchWord == "na")
            assert(state.filteredOverviews.size == 2)
            assert(state.filteredOverviews == MediaMockups.overviews)

        }

    }

    @Test
    fun `search word with no result`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(SearchIntent.DoSearch("spider-man"))

            val state = awaitItem()

            assert(state.searchWord == "spider-man")
            assert(state.filteredOverviews.isEmpty())

        }

    }

}