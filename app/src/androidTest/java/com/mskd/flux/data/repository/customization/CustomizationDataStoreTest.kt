package com.mskd.flux.data.repository.customization

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.features.customization.data.datastore.CustomizationDataStoreImpl
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.utils.UiCommon
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
class CustomizationDataStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var customizationDataStore: CustomizationDataStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context.preferencesDataStoreFile("TEST_CUSTOMIZATION_DATASTORE_NAME")

        val testDataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                tempFolder.newFile("test_customization_datastore_${System.nanoTime()}.preferences_pb")
            }
        )

        customizationDataStore = CustomizationDataStoreImpl(
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

        val defaultState = CustomizationDataStore.State()

        customizationDataStore.flow.test {

            val initialState = awaitItem()

            assert(defaultState.uiTheme == initialState.uiTheme)
            assert(defaultState.color == initialState.color)
            assert(defaultState.waveProgress == initialState.waveProgress)
            assert(defaultState.largeEpisodeImage == initialState.largeEpisodeImage)
            assert(defaultState.itemsPerRow == initialState.itemsPerRow)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_ui_theme() = runTest {

        customizationDataStore.flow.test {
            var state = awaitItem()
            assert(state.uiTheme == UiCommon.THEME.SYSTEM)

            val newTheme = UiCommon.THEME.DARK
            customizationDataStore.setUiTheme(newTheme)
            state = awaitItem()
            assert(state.uiTheme == newTheme)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_color() = runTest {

        customizationDataStore.flow.test {
            var state = awaitItem()
            assert(state.color == null)

            val newColor = 0xFF00FF00.toInt()
            customizationDataStore.setColor(newColor)
            state = awaitItem()
            assert(state.color == newColor)

            customizationDataStore.setColor(null)
            state = awaitItem()
            assert(state.color == null)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_wave_progress() = runTest {

        customizationDataStore.flow.test {
            var state = awaitItem()
            assert(state.waveProgress)

            customizationDataStore.setWaveProgress(false)
            state = awaitItem()
            assert(!state.waveProgress)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_old_blurred_header() = runTest {

        customizationDataStore.flow.test {
            var state = awaitItem()
            assert(!state.oldBlurredHeader)

            customizationDataStore.setOldBlurredHeader(true)
            state = awaitItem()
            assert(state.oldBlurredHeader)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_large_episode_image() = runTest {

        customizationDataStore.flow.test {
            var state = awaitItem()
            assert(!state.largeEpisodeImage)

            customizationDataStore.setLargeEpisodeImage(true)
            state = awaitItem()
            assert(state.largeEpisodeImage)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_items_per_row() = runTest {

        customizationDataStore.flow.test {
            var state = awaitItem()
            assert(state.itemsPerRow == 3)

            customizationDataStore.setItemsPerRow(4)
            state = awaitItem()
            assert(state.itemsPerRow == 4)

            cancelAndConsumeRemainingEvents()
        }

    }

}
