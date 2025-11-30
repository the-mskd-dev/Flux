package com.kaem.flux.screens.home

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.CatalogContent
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.data.repository.UserPreferences
import com.kaem.flux.data.repository.UserRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.MediaOverview
import io.mockk.coEvery
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
    private lateinit var userRepository: UserRepository

    // Mocked flows
    private val libraryFlow = MutableStateFlow(CatalogContent())
    private val dataStoreFlow = MutableStateFlow(UserPreferences())

    override fun setUp() {
        super.setUp()

        catalogRepository = mockk(relaxed = true) {
            every { catalogFlow } returns this@HomeViewModelTest.libraryFlow
        }

        userRepository = mockk(relaxed = true) {
            every { flow } returns this@HomeViewModelTest.dataStoreFlow
        }

        viewModel = HomeViewModel(
            repository = catalogRepository,
            userRepository = userRepository
        )

    }

    @Test
    fun initial_state() = runTest {

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
    fun combine_flows_should_update_state() = runTest {

        // Mock
        val overviews = listOf(MediaMockups.movieOverview, MediaMockups.showOverview)
        val lastWatchedIds = listOf(MediaMockups.showOverview.id)
        val catalogContent = CatalogContent(
            isLoading = false,
            mediaOverviews = overviews
        )
        val dataStore = UserPreferences(
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
    fun should_force_sync_when_manual_sync_requested() = runTest {
        viewModel.handleIntent(HomeIntent.OnSyncTap(manualSync = true))

        advanceUntilIdle()

        coVerify {
            catalogRepository.getCatalog(sync = true)
            userRepository.setSyncTime(any())
        }
    }


    @Test
    fun should_sync_when_last_sync_was_more_than_1_day_ago() = runTest {
        val oldTime = System.currentTimeMillis() - 2.days.inWholeMilliseconds
        coEvery { userRepository.getSyncTime() } returns oldTime

        viewModel = HomeViewModel(
            repository = catalogRepository,
            userRepository = userRepository
        )
        viewModel.handleIntent(HomeIntent.OnSyncTap(manualSync = false))

        advanceUntilIdle()

        coVerify {
            catalogRepository.getCatalog(sync = true)
            userRepository.setSyncTime(any())
        }
    }

    @Test
    fun should_not_sync_when_last_sync_was_less_than_1_day_ago() = runTest {
        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        coEvery { userRepository.getSyncTime() } returns recentTime

        viewModel = HomeViewModel(
            repository = catalogRepository,
            userRepository = userRepository
        )
        viewModel.handleIntent(HomeIntent.OnSyncTap(manualSync = false))

        advanceUntilIdle()

        coVerify {
            catalogRepository.getCatalog(sync = false)
        }
        coVerify(exactly = 0) {
            userRepository.setSyncTime(any())
        }
    }

}