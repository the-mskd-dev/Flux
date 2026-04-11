package com.mskd.flux.screens.unknown

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.mockups.FakeArtworkRepository
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.ScreenState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class UnknownViewModelTest : FunSpec ({

    fluxExtensions()

    lateinit var viewModel: UnknownViewModel
    lateinit var artworkRepository: FakeArtworkRepository

    beforeTest {

        artworkRepository = FakeArtworkRepository()

        viewModel = UnknownViewModel(repository = artworkRepository)

    }

    test("initial state") {

        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.medias shouldBe MediaMockups.unknowns
            initialState.screen shouldBe ScreenState.CONTENT

        }

    }

})