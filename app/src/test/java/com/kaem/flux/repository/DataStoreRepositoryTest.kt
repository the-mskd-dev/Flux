package com.kaem.flux.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import app.cash.turbine.test
import com.google.gson.Gson
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.ui.theme.Ui
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Locale

class DataStoreRepositoryTest : BaseTest() {

    private lateinit var dataStoreRepository: DataStoreRepository
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferences: Preferences
    private lateinit var gson: Gson

    override fun setUp() {
        super.setUp()

        preferences = preferencesOf(
            DataStoreRepository.Keys.WATCHED_IDS to "[1, 2, 3]",
            DataStoreRepository.Keys.PLAYER_BACKWARD to "15",
            DataStoreRepository.Keys.PLAYER_FORWARD to "30",
            DataStoreRepository.Keys.UI_THEME to "DARK",
            DataStoreRepository.Keys.SUBTITLES_LANGUAGE to "fr"
        )

        dataStore = mockk(relaxed = true) {
            every { data } returns flowOf(preferences)
        }
        gson = Gson()
        dataStoreRepository = DataStoreRepository(dataStore, gson)

    }

    @Test
    fun `initial state`() = runTest {

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
    fun `add watched artwork id`() = runTest {
        dataStoreRepository.addWatchedArtwork(4L)

        dataStoreRepository.flow.test {
            val state = awaitItem()

            assert(state.watchedIds.contains(4L))
            assert(state.watchedIds == listOf(4L, 1L, 2L, 3L))

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `remove watched artwork id`() = runTest {
        val idToRemove = 2L

        dataStoreRepository.removeWatchedArtwork(idToRemove)

        coVerify {
            dataStore.edit(any())
        }

        dataStoreRepository.flow.test {
            val state = awaitItem()
            assert(!state.watchedIds.contains(idToRemove))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `get sync time`() = runTest {
        val expectedTime = 123456789L
        val syncPreferences = preferencesOf(
            DataStoreRepository.Keys.LAST_SYNC_TIME to expectedTime.toString()
        )

        every { dataStore.data } returns flowOf(syncPreferences)

        val syncTime = dataStoreRepository.getSyncTime()
        assert(syncTime == expectedTime)
    }

    @Test
    fun `save sync time`() = runTest {
        val timeToSave = 987654321L

        dataStoreRepository.saveSyncTime(timeToSave)

        coVerify {
            dataStore.edit(any())
        }
    }

    @Test
    fun `set player back value`() = runTest {
        val newValue = 20

        dataStoreRepository.setPlayerBackwardValue(newValue)

        coVerify {
            dataStore.edit(any())
        }

        dataStoreRepository.flow.test {
            val state = awaitItem()
            assert(state.playerBackwardValue == newValue)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `set player forward value`() = runTest {
        val newValue = 25

        dataStoreRepository.setPlayerForwardValue(newValue)

        coVerify {
            dataStore.edit(any())
        }

        dataStoreRepository.flow.test {
            val state = awaitItem()
            assert(state.playerForwardValue == newValue)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `get player buttons values`() = runTest {
        val (backward, forward) = dataStoreRepository.getPlayerButtonsValues()

        assert(backward == 15)
        assert(forward == 30)
    }

    @Test
    fun `set ui theme`() = runTest {
        val newTheme = Ui.THEME.LIGHT

        dataStoreRepository.setUiTheme(newTheme)

        coVerify {
            dataStore.edit(any())
        }

        dataStoreRepository.flow.test {
            val state = awaitItem()
            assert(state.uiTheme == newTheme)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `set subtitles language`() = runTest {
        val newLocale = Locale("en")

        dataStoreRepository.setSubtitlesLanguage(newLocale)

        coVerify {
            dataStore.edit(any())
        }

        dataStoreRepository.flow.test {
            val state = awaitItem()
            assert(state.subtitlesLanguage == newLocale)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `get subtitles language`() = runTest {
        val language = dataStoreRepository.getSubtitlesLanguage()
        assert(language == Locale.FRENCH)
    }

}