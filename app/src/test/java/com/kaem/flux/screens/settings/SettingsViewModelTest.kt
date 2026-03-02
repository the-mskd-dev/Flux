package com.kaem.flux.screens.settings

import app.cash.turbine.test
import com.kaem.flux.configs.fluxExtensions
import com.kaem.flux.data.repository.settings.SettingsPreferences
import com.kaem.flux.data.repository.settings.SettingsRepository
import com.kaem.flux.ui.theme.Ui
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class SettingsViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: SettingsViewModel
    lateinit var settingsRepository: SettingsRepository

    val dataStoreFlow = MutableStateFlow(SettingsPreferences())

    beforeTest {

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns dataStoreFlow
        }

        viewModel = SettingsViewModel(settingsRepository = settingsRepository)

    }

    test("initial state") {
        viewModel.uiState.test {
            val initialState = awaitItem()
            initialState.rewindValue shouldBe 10
            initialState.forwardValue shouldBe 10
            initialState.uiTheme shouldBe Ui.THEME.SYSTEM
            initialState.dialogState shouldBe null
        }
    }

    test("show rewind dialog") {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowRewindDialog)

            val state = awaitItem()
            state.dialogState shouldNotBe null
            state.dialogState.shouldBeInstanceOf<SettingsDialogState<Int>>().should {
                it.currentValue.shouldBeInstanceOf<Int>()
            }

        }
    }

    test("show forward dialog") {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowForwardDialog)

            val state = awaitItem()
            state.dialogState shouldNotBe null
            state.dialogState.shouldBeInstanceOf<SettingsDialogState<Int>>().should {
                it.currentValue.shouldBeInstanceOf<Int>()
            }

        }
    }

    test("show ui theme dialog") {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowThemeDialog)

            val state = awaitItem()
            state.dialogState shouldNotBe null
            state.dialogState.shouldBeInstanceOf<SettingsDialogState<Ui.THEME>>().should {
                it.currentValue.shouldBeInstanceOf<Ui.THEME>()
            }
        }
    }

    test("set rewind value") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetRewindValue(20))
            dataStoreFlow.value = dataStoreFlow.value.copy(playerRewindValue = 20)

            val state = awaitItem()

            coVerify { settingsRepository.setPlayerRewindValue(20) }
            state.rewindValue shouldBe 20
            state.dialogState shouldBe null

            cancelAndConsumeRemainingEvents()

        }
    }

    test("set_forward_value") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetForwardValue(20))
            dataStoreFlow.value = dataStoreFlow.value.copy(playerForwardValue = 20)

            val state = awaitItem()

            coVerify { settingsRepository.setPlayerForwardValue(20) }
            state.forwardValue shouldBe 20
            state.dialogState shouldBe null

            cancelAndConsumeRemainingEvents()

        }
    }

    test("set ui theme") {

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetThemeValue(Ui.THEME.DARK))
            dataStoreFlow.value = dataStoreFlow.value.copy(uiTheme = Ui.THEME.DARK)

            val state = awaitItem()

            coVerify { settingsRepository.setUiTheme(Ui.THEME.DARK) }
            state.uiTheme shouldBe Ui.THEME.DARK
            state.dialogState shouldBe null

            cancelAndConsumeRemainingEvents()

        }

    }

})