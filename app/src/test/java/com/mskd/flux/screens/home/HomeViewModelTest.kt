package com.mskd.flux.screens.home

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.mockups.FakeCatalogRepository
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.ScreenState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: HomeViewModel
    lateinit var catalogRepository: FakeCatalogRepository
    lateinit var userRepository: UserRepository

    // Mocked flows
    val dataStoreFlow = MutableStateFlow(UserRepository.State())

    beforeTest {

        catalogRepository = FakeCatalogRepository()

        userRepository = mockk(relaxed = true) {
            every { flow } returns dataStoreFlow
        }

    }

    test("initial state") {

        viewModel = HomeViewModel(
            repository = catalogRepository,
            userRepository = userRepository
        )

        viewModel.uiState.test {
            val initialState = awaitItem()
            initialState.screenState shouldBe ScreenState.CONTENT
            initialState.artworks shouldBe MediaMockups.artworks
            initialState.lastWatchedMediaIds shouldBe emptyList()
            initialState.isRefreshing shouldBe false

            cancelAndConsumeRemainingEvents()
        }

    }

    test("combine flows should update state") {

        viewModel = HomeViewModel(
            repository = catalogRepository,
            userRepository = userRepository
        )

        // Mock
        val artworks = listOf(MediaMockups.movieArtwork, MediaMockups.showArtwork)
        val lastWatchedIds = listOf(MediaMockups.showArtwork.id)
        val dataStore = UserRepository.State(
            recentlyWatchedIds = lastWatchedIds
        )

        viewModel.uiState.test {

            awaitItem() // Ignore initial state

            catalogRepository.syncCatalog()
            dataStoreFlow.value = dataStore

            val updatedState = expectMostRecentItem()

            updatedState.screenState shouldBe ScreenState.CONTENT
            updatedState.artworks shouldBe artworks
            updatedState.lastWatchedMediaIds shouldBe lastWatchedIds
            updatedState.isRefreshing shouldBe false

            cancelAndConsumeRemainingEvents()
        }
    }

    test("should force sync when manual sync requested") {

        viewModel = HomeViewModel(
            repository = catalogRepository,
            userRepository = userRepository
        )

        viewModel.handleIntent(HomeIntent.SyncCatalog)

        coVerify {
            catalogRepository.syncCatalog()
            userRepository.setSyncTime(any())
        }
    }


    test("should sync when last sync was more than 1 day ago") {
        val oldTime = System.currentTimeMillis() - 2.days.inWholeMilliseconds
        coEvery { userRepository.getSyncTime() } returns oldTime

        viewModel = HomeViewModel(
            repository = catalogRepository,
            userRepository = userRepository
        )

        coVerify(exactly = 1) {
            catalogRepository.syncCatalog()
            userRepository.setSyncTime(any())
        }
    }

    test("should not sync when last sync was less than 1 day ago") {
        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        coEvery { userRepository.getSyncTime() } returns recentTime

        viewModel = HomeViewModel(
            repository = catalogRepository,
            userRepository = userRepository
        )

        coVerify(exactly = 0) {
            catalogRepository.syncCatalog()
            userRepository.setSyncTime(any())
        }
    }

})