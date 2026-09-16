package com.mskd.flux.features.privateFolder.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.setArtworkPrivacy.SetArtworkPrivacyUseCase
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class PrivateFolderViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: PrivateFolderViewModel
    lateinit var database: DatabaseRepository
    lateinit var privateFolderDataStore: PrivateFolderDataStore
    lateinit var setArtworkPrivacy: SetArtworkPrivacyUseCase

    val privateFolderFlow = MutableStateFlow(PrivateFolderDataStore.State())

    beforeTest {

        database = FakeDatabaseRepository()
        setArtworkPrivacy = mockk(relaxed = true)

        privateFolderDataStore = mockk(relaxed = true) {
            every { flow } returns privateFolderFlow
        }

        viewModel = PrivateFolderViewModel(
            database = database,
            privateFolderDataStore = privateFolderDataStore,
            setArtworkPrivacy = setArtworkPrivacy
        )

    }

    test("initial state - locked screen when folder is enabled") {
        privateFolderFlow.value = PrivateFolderDataStore.State(enabled = true)

        viewModel.uiState.test {
            val state = awaitItem()
            state.screen shouldBe State.Content(Unit)
            state.locked shouldBe true
            state.pinError shouldBe false
            state.artworks shouldBe emptyList()
        }
    }

    test("folder is unlocked when disabled") {
        privateFolderFlow.value = PrivateFolderDataStore.State(enabled = false)

        viewModel.uiState.test {
            val state = awaitItem()
            state.locked shouldBe false
        }
    }

    test("submit correct pin unlocks the folder") {
        privateFolderFlow.value = PrivateFolderDataStore.State(enabled = true)
        coEvery { privateFolderDataStore.verifyPin("1234") } returns true

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PrivateFolderIntent.SubmitPin(pin = "1234"))

            val state = awaitItem()
            state.locked shouldBe false
            state.pinError shouldBe false
        }
    }

    test("submit wrong pin keeps the folder locked and shows error") {
        privateFolderFlow.value = PrivateFolderDataStore.State(enabled = true)
        coEvery { privateFolderDataStore.verifyPin("0000") } returns false

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PrivateFolderIntent.SubmitPin(pin = "0000"))

            val state = awaitItem()
            state.locked shouldBe true
            state.pinError shouldBe true
        }
    }

    test("disabled private folder - no private artworks are listed") {
        privateFolderFlow.value = PrivateFolderDataStore.State(enabled = false)

        viewModel.uiState.test {
            val state = awaitItem()
            state.locked shouldBe false
            state.artworks shouldBe emptyList() // No artwork is private in mockups
        }
    }

    test("on artwork tap - movie") {
        viewModel.event.test {
            viewModel.handleIntent(PrivateFolderIntent.OnArtworkTap(artwork = MediaMockups.movieArtwork))
            awaitItem() shouldBe PrivateFolderEvent.NavigateToMovie(artworkId = MediaMockups.movieArtwork.id, rgb = null)
        }
    }

    test("on artwork tap - show") {
        viewModel.event.test {
            viewModel.handleIntent(PrivateFolderIntent.OnArtworkTap(artwork = MediaMockups.showArtwork))
            awaitItem() shouldBe PrivateFolderEvent.NavigateToShow(artworkId = MediaMockups.showArtwork.id, rgb = null)
        }
    }

    test("on back tap") {
        viewModel.event.test {
            viewModel.handleIntent(PrivateFolderIntent.OnBackTap)
            awaitItem() shouldBe PrivateFolderEvent.BackToPreviousScreen
        }
    }

    test("remove artwork from private folder") {
        val artwork = MediaMockups.movieArtwork

        viewModel.handleIntent(PrivateFolderIntent.RemoveFromPrivateFolder(artwork = artwork))

        coVerify { setArtworkPrivacy(artworkId = artwork.id, isPrivate = false) }
    }

})
