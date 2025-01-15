package com.kaem.flux.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.gson.Gson
import com.kaem.flux.ui.theme.Ui
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale


//TODO: See https://medium.com/androiddevelopers/datastore-and-testing-edf7ae8df3d8

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val testDispatcher = StandardTestDispatcher()
    private val testDataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("TEST_DATASTORE_NAME") }
    )
    private val dataStoreRepository = DataStoreRepository(
        dataStore = testDataStore,
        gson = Gson()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state() = runTest {

        val defaultDataStore = FluxDataStore()

        dataStoreRepository.flow.test {

            val initialState = awaitItem()

            assert(defaultDataStore.uiTheme == initialState.uiTheme)
            assert(defaultDataStore.subtitlesLanguage == initialState.subtitlesLanguage)
            assert(defaultDataStore.playerBackwardValue == initialState.playerBackwardValue)
            assert(defaultDataStore.playerForwardValue == initialState.playerForwardValue)
            assert(defaultDataStore.watchedIds == initialState.watchedIds)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun add_and_remove_watched_artwork_id() = runTest {

        val idTest = 4L

        dataStoreRepository.flow.test {
            var state = awaitItem()

            assert(state.watchedIds.isEmpty())

            dataStoreRepository.addWatchedArtwork(idTest)
            advanceUntilIdle()
            state = awaitItem()
            assert(state.watchedIds.contains(idTest))

            dataStoreRepository.removeWatchedArtwork(idTest)
            advanceUntilIdle()
            state = awaitItem()
            assert(!state.watchedIds.contains(idTest))

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_sync_time() = runTest {
        val expectedTime = 123456789L
        val syncPreferences = preferencesOf(
            DataStoreRepository.Keys.LAST_SYNC_TIME to expectedTime.toString()
        )


        val syncTime = dataStoreRepository.getSyncTime()
        assert(syncTime == expectedTime)
    }

    @Test
    fun save_sync_time() = runTest {
        val timeToSave = 987654321L

        dataStoreRepository.saveSyncTime(timeToSave)

    }

    @Test
    fun set_player_back_value() = runTest {
        val newValue = 20

        dataStoreRepository.setPlayerBackwardValue(newValue)


        dataStoreRepository.flow.test {
            val state = awaitItem()
            assert(state.playerBackwardValue == newValue)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun set_player_forward_value() = runTest {
        val newValue = 30

        dataStoreRepository.setPlayerForwardValue(newValue)

        dataStoreRepository.flow.test {
            val state = awaitItem()
            assert(state.playerForwardValue == newValue)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun get_player_buttons_values() = runTest {
        val (backward, forward) = dataStoreRepository.getPlayerButtonsValues()

        assert(backward == 15)
        assert(forward == 30)
    }

    @Test
    fun set_ui_theme() = runTest {
        val newTheme = Ui.THEME.LIGHT

        dataStoreRepository.setUiTheme(newTheme)

        dataStoreRepository.flow.test {
            val state = awaitItem()
            assert(state.uiTheme == newTheme)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun set_subtitles_language() = runTest {
        val newLocale = Locale("en")

        dataStoreRepository.setSubtitlesLanguage(newLocale)

        dataStoreRepository.flow.test {
            val state = awaitItem()
            assert(state.subtitlesLanguage == newLocale)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun get_subtitles_language() = runTest {
        val language = dataStoreRepository.getSubtitlesLanguage()
        assert(language == Locale.FRENCH)
    }

}