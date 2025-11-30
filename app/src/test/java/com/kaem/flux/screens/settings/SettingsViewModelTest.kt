package com.kaem.flux.screens.settings

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.SettingsPreferences
import com.kaem.flux.data.repository.SettingsRepository
import com.kaem.flux.ui.theme.Ui
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Locale

class SettingsViewModelTest : BaseTest() {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var settingsRepository: SettingsRepository

    private val dataStoreFlow = MutableStateFlow(SettingsPreferences())

    override fun setUp() {
        super.setUp()

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns dataStoreFlow
        }

        viewModel = SettingsViewModel(settingsRepository = settingsRepository)
    }

    @Test
    fun initial_state() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assert(10 == initialState.backwardValue)
            assert(10 == initialState.forwardValue)
            assert(Ui.THEME.SYSTEM == initialState.uiTheme)
            assert(Locale.getDefault() == initialState.subtitlesLanguage)
            assert(initialState.dialogState == null)
        }
    }

    @Test
    fun show_backward_dialog() = runTest {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowBackwardDialog)

            val state = awaitItem()
            assert(state.dialogState != null)

        }
    }

    @Test
    fun show_forward_dialog() = runTest {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowForwardDialog)

            val state = awaitItem()
            assert(state.dialogState != null)

        }
    }

    @Test
    fun show_ui_theme_dialog() = runTest {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowThemeDialog)

            val state = awaitItem()
            assert(state.dialogState != null)

        }
    }

    @Test
    fun show_subtitles_language_dialog() = runTest {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowSubtitlesDialog)

            val state = awaitItem()
            assert(state.dialogState != null)

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun set_backward_value() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetBackwardValue(20))
            dataStoreFlow.value = dataStoreFlow.value.copy(playerBackwardValue = 20)
            advanceUntilIdle()

            val state = awaitItem()

            coVerify { settingsRepository.setPlayerBackwardValue(20) }
            assert(20 == state.backwardValue)
            assert(state.dialogState == null)

            cancelAndConsumeRemainingEvents()

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun set_forward_value() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetForwardValue(20))
            dataStoreFlow.value = dataStoreFlow.value.copy(playerForwardValue = 20)
            advanceUntilIdle()

            val state = awaitItem()

            coVerify { settingsRepository.setPlayerForwardValue(20) }
            assert(20 == state.forwardValue)
            assert(state.dialogState == null)

            cancelAndConsumeRemainingEvents()

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun set_ui_theme() = runTest {

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetThemeValue(Ui.THEME.DARK))
            dataStoreFlow.value = dataStoreFlow.value.copy(uiTheme = Ui.THEME.DARK)
            advanceUntilIdle()

            val state = awaitItem()

            coVerify { settingsRepository.setUiTheme(Ui.THEME.DARK) }
            assert(Ui.THEME.DARK == state.uiTheme)
            assert(state.dialogState == null)

            cancelAndConsumeRemainingEvents()

        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun set_subtitles_language() = runTest {

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetSubtitlesValue(Locale.ENGLISH))
            dataStoreFlow.value = dataStoreFlow.value.copy(subtitlesLanguage = Locale.ENGLISH)
            advanceUntilIdle()

            val state = awaitItem()

            coVerify { settingsRepository.setSubtitlesLanguage(Locale.ENGLISH) }
            assert(Locale.ENGLISH == state.subtitlesLanguage)
            assert(state.dialogState == null)

            cancelAndConsumeRemainingEvents()

        }

    }

}