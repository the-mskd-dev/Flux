package com.kaem.flux.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import app.cash.turbine.test
import com.google.gson.Gson
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.ui.theme.Ui
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
        //TODO
    }

    @Test
    fun `remove watched artwork id`() = runTest {
        //TODO
    }

    @Test
    fun `get sync time`() = runTest {
        //TODO
    }

    @Test
    fun `save sync time`() = runTest {
        //TODO
    }

    @Test
    fun `set player back value`() = runTest {
        //TODO
    }

    @Test
    fun `set player forward value`() = runTest {
        //TODO
    }

    @Test
    fun `get player buttons values`() = runTest {
        //TODO
    }

    @Test
    fun `set ui theme`() = runTest {
        //TODO
    }

    @Test
    fun `set subtitles language`() = runTest {
        //TODO
    }

    @Test
    fun `get subtitles language`() = runTest {
        //TODO
    }

}