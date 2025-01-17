package com.kaem.flux.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.Locale


//TODO: See https://medium.com/androiddevelopers/datastore-and-testing-edf7ae8df3d8

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dataStoreRepository: DataStoreRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context.preferencesDataStoreFile("TEST_DATASTORE_NAME")

        val testDataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                tempFolder.newFile("test_datastore_${System.nanoTime()}.preferences_pb")
            }
        )

        dataStoreRepository = DataStoreRepository(
            dataStore = testDataStore,
            gson = Gson()
        )

        testDispatcher.scheduler.advanceUntilIdle()
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
            state = awaitItem()
            assert(state.watchedIds.contains(idTest))

            dataStoreRepository.removeWatchedArtwork(idTest)
            state = awaitItem()
            assert(!state.watchedIds.contains(idTest))

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_sync_time() = runTest {

        var syncTime = dataStoreRepository.getSyncTime()
        assert(syncTime == 0L)

        val testTime = 123456789L
        dataStoreRepository.setSyncTime(testTime)

        syncTime = dataStoreRepository.getSyncTime()

        assert(syncTime == testTime)
    }

    @Test
    fun get_and_set_player_back_value() = runTest {

        dataStoreRepository.flow.test {

            var state = awaitItem()

            assert(FluxDataStore().playerBackwardValue == state.playerBackwardValue)

            val newValue = 20
            dataStoreRepository.setPlayerBackwardValue(newValue)
            state = awaitItem()

            val blockingValue = dataStoreRepository.getPlayerButtonsValues().first
            assert(newValue == state.playerBackwardValue)
            assert(newValue == blockingValue)

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun get_and_set_player_forward_value() = runTest {

        dataStoreRepository.flow.test {

            var state = awaitItem()

            assert(FluxDataStore().playerForwardValue == state.playerForwardValue)

            val newValue = 20
            dataStoreRepository.setPlayerForwardValue(newValue)
            state = awaitItem()

            val blockingValue = dataStoreRepository.getPlayerButtonsValues().second
            assert(newValue == state.playerForwardValue)
            assert(newValue == blockingValue)

            cancelAndConsumeRemainingEvents()

        }

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
    fun get_and_set_subtitles_language() = runTest {

        val language = dataStoreRepository.getSubtitlesLanguage()
        assert(language == Locale.getDefault())

        dataStoreRepository.flow.test {
            awaitItem()

            val newLocale = Locale.JAPANESE
            dataStoreRepository.setSubtitlesLanguage(newLocale)
            val state = awaitItem()
            assert(state.subtitlesLanguage == newLocale)

            cancelAndConsumeRemainingEvents()

        }

    }

}