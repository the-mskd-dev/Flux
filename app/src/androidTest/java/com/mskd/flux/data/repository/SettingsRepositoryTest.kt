package com.mskd.flux.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.settings.SettingsRepositoryImpl
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

        settingsRepository.flow.test {

            val initialState = awaitItem()

            assert(initialState.subtitlesLanguage == Locale.getDefault())
            assert(initialState.audioLanguage == Locale.getDefault())
            assert(initialState.playerRewindValue == 10)
            assert(initialState.playerForwardValue == 10)
            assert(!initialState.externalPlayer)
            assert(initialState.autoKeyboard)
            assert(initialState.dataLanguage == null)
            assert(initialState.prefetchImages)

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

    @Test
    fun get_and_set_audio_language() = runTest {

        val language = settingsRepository.flow.first().audioLanguage
        assert(language == Locale.getDefault())

        settingsRepository.flow.test {
            awaitItem()

            val newLocale = Locale.FRENCH
            settingsRepository.setAudioLanguage(newLocale)
            val state = awaitItem()
            assert(state.audioLanguage == newLocale)

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun get_and_set_external_player() = runTest {

        settingsRepository.flow.test {
            var state = awaitItem()
            assert(!state.externalPlayer)

            settingsRepository.setExternalPlayer(true)
            state = awaitItem()
            assert(state.externalPlayer)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_auto_keyboard() = runTest {

        settingsRepository.flow.test {
            var state = awaitItem()
            assert(state.autoKeyboard)

            settingsRepository.setAutoKeyboard(false)
            state = awaitItem()
            assert(!state.autoKeyboard)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_prefetch_images() = runTest {

        settingsRepository.flow.test {
            var state = awaitItem()
            assert(state.prefetchImages)

            settingsRepository.setPrefetchImages(false)
            state = awaitItem()
            assert(!state.prefetchImages)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_data_language() = runTest {

        settingsRepository.flow.test {
            var state = awaitItem()
            assert(state.dataLanguage == null)

            val newLocale = Locale.GERMAN
            settingsRepository.setDataLanguage(newLocale)
            state = awaitItem()
            assert(state.dataLanguage == newLocale)

            settingsRepository.setDataLanguage(null)
            state = awaitItem()
            assert(state.dataLanguage == null)

            cancelAndConsumeRemainingEvents()
        }

    }

}