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

        dataStoreRepository.flow.test {

            val initialState = awaitItem()

            assert(initialState.watchedIds.isNotEmpty())
            assert(listOf(1L, 2L, 3L) == initialState.watchedIds)
            assert(15 == initialState.playerBackwardValue)
            assert(30 == initialState.playerForwardValue)
            assert(Ui.THEME.DARK == initialState.uiTheme)
            assert(Locale.FRENCH == initialState.subtitlesLanguage)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun add_watched_artwork_id() = runTest {
        dataStoreRepository.addWatchedArtwork(4L)

        dataStoreRepository.flow.test {
            val state = awaitItem()

            assert(state.watchedIds.contains(4L))
            assert(state.watchedIds == listOf(4L, 1L, 2L, 3L))

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun remove_watched_artwork_id() = runTest {
        val idToRemove = 2L

        dataStoreRepository.removeWatchedArtwork(idToRemove)

        dataStoreRepository.flow.test {
            val state = awaitItem()
            assert(!state.watchedIds.contains(idToRemove))
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