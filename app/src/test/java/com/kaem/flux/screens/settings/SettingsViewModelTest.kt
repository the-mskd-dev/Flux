package com.kaem.flux.screens.settings

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.FluxDataStore
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
    private lateinit var dataStoreRepository: DataStoreRepository

    private val dataStoreFlow = MutableStateFlow(FluxDataStore())

    override fun setUp() {
        super.setUp()

        dataStoreRepository = mockk(relaxed = true) {
            every { flow } returns dataStoreFlow
        }

        viewModel = SettingsViewModel(dataStoreRepository)
    }

    @Test
    fun `initial state`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assert(10 == initialState.backwardValue)
            assert(!initialState.showBackwardDialog)
            assert(10 == initialState.forwardValue)
            assert(!initialState.showForwardDialog)
            assert(Ui.THEME.SYSTEM == initialState.uiTheme)
            assert(!initialState.showUiThemeDialog)
            assert(Locale.getDefault() == initialState.subtitlesLanguage)
            assert(!initialState.showSubtitlesLanguage)
        }
    }

    @Test
    fun `show backward dialog`() = runTest {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.BackwardDialog(true))

            val state = awaitItem()
            assert(state.showBackwardDialog)

        }
    }

    @Test
    fun `show forward dialog`() = runTest {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ForwardDialog(true))

            val state = awaitItem()
            assert(state.showForwardDialog)

        }
    }

    @Test
    fun `show ui theme dialog`() = runTest {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ThemeDialog(true))

            val state = awaitItem()
            assert(state.showUiThemeDialog)

        }
    }

    @Test
    fun `show subtitles language dialog`() = runTest {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.SubtitlesDialog(true))

            val state = awaitItem()
            assert(state.showSubtitlesLanguage)

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `set backward value`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetBackwardValue(20))
            dataStoreFlow.value = dataStoreFlow.value.copy(playerBackwardValue = 20)
            advanceUntilIdle()

            val state = awaitItem()

            coVerify { dataStoreRepository.setPlayerBackwardValue(20) }
            assert(20 == state.backwardValue)

            cancelAndConsumeRemainingEvents()

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `set forward value`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetForwardValue(20))
            dataStoreFlow.value = dataStoreFlow.value.copy(playerForwardValue = 20)
            advanceUntilIdle()

            val state = awaitItem()

            coVerify { dataStoreRepository.setPlayerForwardValue(20) }
            assert(20 == state.forwardValue)

            cancelAndConsumeRemainingEvents()

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `set ui theme`() = runTest {

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetThemeValue(Ui.THEME.DARK))
            dataStoreFlow.value = dataStoreFlow.value.copy(uiTheme = Ui.THEME.DARK)
            advanceUntilIdle()

            val state = awaitItem()

            coVerify { dataStoreRepository.setUiTheme(Ui.THEME.DARK) }
            assert(Ui.THEME.DARK == state.uiTheme)

            cancelAndConsumeRemainingEvents()

        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `set subtitles language`() = runTest {

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetSubtitlesValue(Locale.ENGLISH))
            dataStoreFlow.value = dataStoreFlow.value.copy(subtitlesLanguage = Locale.ENGLISH)
            advanceUntilIdle()

            val state = awaitItem()

            coVerify { dataStoreRepository.setSubtitlesLanguage(Locale.ENGLISH) }
            assert(Locale.ENGLISH == state.subtitlesLanguage)

            cancelAndConsumeRemainingEvents()

        }

    }

}