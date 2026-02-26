package com.kaem.flux.screens.home

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.catalog.CatalogContent
import com.kaem.flux.data.repository.catalog.CatalogRepository
import com.kaem.flux.data.repository.firebase.FirebaseRepository
import com.kaem.flux.data.repository.user.UserPreferences
import com.kaem.flux.data.repository.user.UserRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.remoteConfig.Message
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

    private lateinit var firebaseRepository: FirebaseRepository

    // Mocked flows
    private val libraryFlow = MutableStateFlow(CatalogContent())
    private val dataStoreFlow = MutableStateFlow(UserPreferences())

    private val messageFlow = MutableStateFlow<Message?>(null)

    override fun setUp() {
        super.setUp()

        catalogRepository = mockk(relaxed = true) {
            every { catalogFlow } returns this@HomeViewModelTest.libraryFlow
        }

        userRepository = mockk(relaxed = true) {
            every { flow } returns this@HomeViewModelTest.dataStoreFlow
        }

        firebaseRepository = mockk(relaxed = true) {
            every { message } returns this@HomeViewModelTest.messageFlow
        }

        viewModel = HomeViewModel(
            repository = catalogRepository,
            userRepository = userRepository,
            firebaseRepository = firebaseRepository
        )

    }

    @Test
    fun initial_state() = runTest {

        viewModel.uiState.test {
            val initialState = awaitItem()
            assert(ScreenState.LOADING == initialState.screenState)
            assert(emptyList<Artwork>() == initialState.artworks)
            assert(emptyList<Long>() == initialState.lastWatchedMediaIds)
            assert(initialState.isRefreshing)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun combine_flows_should_update_state() = runTest {

        // Mock
        val artworks = listOf(MediaMockups.movieArtwork, MediaMockups.showArtwork)
        val lastWatchedIds = listOf(MediaMockups.showArtwork.id)
        val catalogContent = CatalogContent(
            isLoading = false,
            artworks = artworks
        )
        val dataStore = UserPreferences(
            recentlyWatchedIds = lastWatchedIds
        )

        viewModel.uiState.test {
            awaitItem() // Ignore initial state

            libraryFlow.value = catalogContent
            dataStoreFlow.value = dataStore

            val updatedState = awaitItem()

            assert(ScreenState.CONTENT == updatedState.screenState)
            assert(artworks == updatedState.artworks)
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
            userRepository = userRepository,
            firebaseRepository = firebaseRepository
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
            userRepository = userRepository,
            firebaseRepository = firebaseRepository
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