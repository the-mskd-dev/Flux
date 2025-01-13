package com.kaem.flux.screens.search

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.LibraryContent
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.mockups.ArtworkMockups
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchViewModelTest : BaseTest() {

    private lateinit var viewModel: SearchViewModel
    private lateinit var libraryRepository: LibraryRepository

    // Mocked data
    private val libraryFlow = MutableStateFlow(LibraryContent(
        artworkOverviews = ArtworkMockups.overviews
    ))

    override fun setUp() {
        super.setUp()

        libraryRepository = mockk(relaxed = true) {
            every { libraryFlow } returns this@SearchViewModelTest.libraryFlow
        }


        viewModel = SearchViewModel(libraryRepository)

    }

    @Test
    fun `initial state`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()

            assert(initialState.searchWord == "")
            assert(initialState.overviews == ArtworkMockups.overviews)

            cancelAndConsumeRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `search word with result`() = runTest {

        viewModel.updateSearchWord("nar")

        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem()
            val state = awaitItem()

            assert(state.searchWord == "nar")
            assert(state.filteredOverviews.size == 1)
            assert(state.filteredOverviews.any { it.title.equals("naruto", ignoreCase = true) })

            cancelAndConsumeRemainingEvents()
        }

    }
}