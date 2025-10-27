package com.kaem.flux.screens.home

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.CatalogContent
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.FluxDataStore
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.MediaOverview
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : BaseTest() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var catalogRepository: CatalogRepository
    private lateinit var dataStoreRepository: DataStoreRepository

    // Mocked flows
    private val libraryFlow = MutableStateFlow(CatalogContent())
    private val dataStoreFlow = MutableStateFlow(FluxDataStore())

    override fun setUp() {
        super.setUp()

        catalogRepository = mockk(relaxed = true) {
            every { catalogFlow } returns this@HomeViewModelTest.libraryFlow
        }

        dataStoreRepository = mockk(relaxed = true) {
            every { flow } returns this@HomeViewModelTest.dataStoreFlow
            every { getSyncTime() } returns 0L
        }

        viewModel = HomeViewModel(catalogRepository, dataStoreRepository)
    }

    @Test
    fun `initial state`() = runTest {

        viewModel.uiState.test {
            val initialState = awaitItem()
            assert(ScreenState.LOADING == initialState.screenState)
            assert(emptyList<MediaOverview>() == initialState.overviews)
            assert(emptyList<Long>() == initialState.lastWatchedMediaIds)
            assert(initialState.isRefreshing)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun `combine flows should update state`() = runTest {

        // Mock
        val overviews = listOf(MediaMockups.movieOverview, MediaMockups.showOverview)
        val lastWatchedIds = listOf(MediaMockups.showOverview.id)
        val catalogContent = CatalogContent(
            isLoading = false,
            mediaOverviews = overviews
        )
        val dataStore = FluxDataStore(
            watchedIds = lastWatchedIds
        )

        viewModel.uiState.test {
            awaitItem() // Ignore initial state

            libraryFlow.value = catalogContent
            dataStoreFlow.value = dataStore

            val updatedState = awaitItem()

            assert(ScreenState.CONTENT == updatedState.screenState)
            assert(overviews == updatedState.overviews)
            assert(lastWatchedIds == updatedState.lastWatchedMediaIds)
            assert(!updatedState.isRefreshing)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `should force sync when manual sync requested`() = runTest {
        viewModel.handleIntent(HomeIntent.OnSyncTap(manualSync = true))

        advanceUntilIdle()

        coVerify {
            catalogRepository.getCatalog(sync = true)
            dataStoreRepository.setSyncTime(any())
        }
    }


    @Test
    fun `should sync when last sync was more than 1 day ago`() = runTest {
        val oldTime = System.currentTimeMillis() - 2.days.inWholeMilliseconds
        every { dataStoreRepository.getSyncTime() } returns oldTime

        viewModel = HomeViewModel(catalogRepository, dataStoreRepository)
        viewModel.handleIntent(HomeIntent.OnSyncTap(manualSync = false))

        advanceUntilIdle()

        coVerify {
            catalogRepository.getCatalog(sync = true)
            dataStoreRepository.setSyncTime(any())
        }
    }

    @Test
    fun `should not sync when last sync was less than 1 day ago`() = runTest {
        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        every { dataStoreRepository.getSyncTime() } returns recentTime

        viewModel = HomeViewModel(catalogRepository, dataStoreRepository)
        viewModel.handleIntent(HomeIntent.OnSyncTap(manualSync = false))

        advanceUntilIdle()

        coVerify {
            catalogRepository.getCatalog(sync = false)
        }
        coVerify(exactly = 0) {
            dataStoreRepository.setSyncTime(any())
        }
    }

}