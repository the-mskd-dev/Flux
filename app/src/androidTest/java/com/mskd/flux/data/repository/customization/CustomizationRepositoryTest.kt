package com.mskd.flux.data.repository.customization

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.ui.theme.Ui
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
@MediumTest
class CustomizationRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var customizationRepository: CustomizationRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context.preferencesDataStoreFile("TEST_CUSTOMIZATION_DATASTORE_NAME")

        val testDataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                tempFolder.newFile("test_customization_datastore_${System.nanoTime()}.preferences_pb")
            }
        )

        customizationRepository = CustomizationRepositoryImpl(
            customizationDataStore = testDataStore
        )

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state() = runTest {

        val defaultState = CustomizationRepository.State()

        customizationRepository.flow.test {

            val initialState = awaitItem()

            assert(defaultState.uiTheme == initialState.uiTheme)
            assert(defaultState.color == initialState.color)
            assert(defaultState.waveProgress == initialState.waveProgress)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_ui_theme() = runTest {

        customizationRepository.flow.test {
            var state = awaitItem()
            assert(state.uiTheme == Ui.THEME.SYSTEM)

            val newTheme = Ui.THEME.DARK
            customizationRepository.setUiTheme(newTheme)
            state = awaitItem()
            assert(state.uiTheme == newTheme)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_color() = runTest {

        customizationRepository.flow.test {
            var state = awaitItem()
            assert(state.color == null)

            val newColor = 0xFF00FF00.toInt()
            customizationRepository.setColor(newColor)
            state = awaitItem()
            assert(state.color == newColor)

            customizationRepository.setColor(null)
            state = awaitItem()
            assert(state.color == null)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_wave_progress() = runTest {

        customizationRepository.flow.test {
            var state = awaitItem()
            assert(state.waveProgress)

            customizationRepository.setWaveProgress(false)
            state = awaitItem()
            assert(!state.waveProgress)

            cancelAndConsumeRemainingEvents()
        }

    }

}
