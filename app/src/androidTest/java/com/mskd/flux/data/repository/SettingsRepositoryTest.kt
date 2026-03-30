package com.mskd.flux.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.settings.SettingsRepositoryImpl
import com.mskd.flux.ui.theme.Ui
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@MediumTest
class SettingsRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context.preferencesDataStoreFile("TEST_DATASTORE_NAME")

        val testDataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                tempFolder.newFile("test_datastore_${System.nanoTime()}.preferences_pb")
            }
        )

        settingsRepository = SettingsRepositoryImpl(
            settingsDataStore = testDataStore
        )

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state() = runTest {

        val defaultDataStore = SettingsRepository.State()

        settingsRepository.flow.test {

            val initialState = awaitItem()

            assert(defaultDataStore.uiTheme == initialState.uiTheme)
            assert(defaultDataStore.subtitlesLanguage == initialState.subtitlesLanguage)
            assert(defaultDataStore.playerRewindValue == initialState.playerRewindValue)
            assert(defaultDataStore.playerForwardValue == initialState.playerForwardValue)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_player_back_value() = runTest {

        settingsRepository.flow.test {

            var state = awaitItem()

            assert(SettingsRepository.State().playerRewindValue == state.playerRewindValue)

            val newValue = 20
            settingsRepository.setPlayerRewindValue(newValue)
            state = awaitItem()

            val blockingValue = settingsRepository.flow.first().playerRewindValue
            assert(newValue == state.playerRewindValue)
            assert(newValue == blockingValue)

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun get_and_set_player_forward_value() = runTest {

        settingsRepository.flow.test {

            var state = awaitItem()

            assert(SettingsRepository.State().playerForwardValue == state.playerForwardValue)

            val newValue = 20
            settingsRepository.setPlayerForwardValue(newValue)
            state = awaitItem()

            val blockingValue = settingsRepository.flow.first().playerForwardValue
            assert(newValue == state.playerForwardValue)
            assert(newValue == blockingValue)

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun set_ui_theme() = runTest {

        val newTheme = Ui.THEME.LIGHT

        settingsRepository.setUiTheme(newTheme)

        settingsRepository.flow.test {
            val state = awaitItem()
            assert(state.uiTheme == newTheme)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun get_and_set_subtitles_language() = runTest {

        val language = settingsRepository.flow.first().subtitlesLanguage
        assert(language == Locale.getDefault())

        settingsRepository.flow.test {
            awaitItem()

            val newLocale = Locale.JAPANESE
            settingsRepository.setSubtitlesLanguage(newLocale)
            val state = awaitItem()
            assert(state.subtitlesLanguage == newLocale)

            cancelAndConsumeRemainingEvents()

        }

    }

}