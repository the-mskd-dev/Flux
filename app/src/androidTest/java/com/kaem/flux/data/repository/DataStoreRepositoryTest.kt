package com.kaem.flux.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import com.google.gson.Gson
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreRepositoryTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var gson: Gson
    private lateinit var repository: DataStoreRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {

        dataStore = mockk()
        gson = Gson()
        repository = DataStoreRepository(dataStore, gson)

        Dispatchers.setMain(testDispatcher)

    }

    @Test
    fun testFlowEmission() = runTest {
        // Préparation des données
        val mockPreferences = preferencesOf(
            DataStoreRepository.Keys.LAST_WATCHED_IDS to "[1, 2, 3]",
            DataStoreRepository.Keys.PLAYER_BACKWARD to "15",
            DataStoreRepository.Keys.PLAYER_FORWARD to "30",
            DataStoreRepository.Keys.UI_THEME to "DARK",
            DataStoreRepository.Keys.SUBTITLES_LANGUAGE to "fr"
        )

        coEvery { dataStore.data } returns flowOf(mockPreferences)

        // Test
        repository.flow.test {
            val emission = awaitItem()

            assertEquals(listOf(1L, 2L, 3L), emission.lastWatchedIds)
            assertEquals(15, emission.playerBackwardValue)
            assertEquals(30, emission.playerForwardValue)
            assertEquals(Ui.THEME.DARK, emission.uiTheme)
            assertEquals(Locale("fr"), emission.subtitlesLanguage)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `test addWatchedArtwork`() = testScope.runTest {
        // Préparation
        val initialIds = "[1, 2, 3]"
        val mockPreferences = preferencesOf(
            DataStoreRepository.Keys.LAST_WATCHED_IDS to initialIds
        )

        coEvery { dataStore.data } returns flowOf(mockPreferences)
        coEvery { dataStore.edit(any()) } returns mockPreferences

        // Test
        repository.addWatchedArtwork(4L)

        // Vérification
        coVerify {
            dataStore.edit(match {
                // Vérifie que la nouvelle liste contient bien le nouvel ID en première position
                val newList = gson.fromJson<List<Long>>(initialIds, List::class.java)
                newList.first() == 4L
            })
        }
    }

    @Test
    fun `test removeWatchedArtwork`() = testScope.runTest {
        // Préparation
        val initialIds = "[1, 2, 3]"
        val mockPreferences = preferencesOf(
            DataStoreRepository.Keys.LAST_WATCHED_IDS to initialIds
        )

        coEvery { dataStore.data } returns flowOf(mockPreferences)
        coEvery { dataStore.edit(any()) } returns mockPreferences

        // Test
        repository.removeWatchedArtwork(2L)

        // Vérification
        coVerify {
            dataStore.edit(match {
                val newList = gson.fromJson<List<Long>>(initialIds, List::class.java)
                !newList.contains(2L)
            })
        }
    }

    @Test
    fun `test setPlayerButtonsValues`() = testScope.runTest {
        // Préparation
        val mockPreferences = preferencesOf(
            DataStoreRepository.Keys.PLAYER_BACKWARD to "5",
            DataStoreRepository.Keys.PLAYER_FORWARD to "10"
        )

        coEvery { dataStore.data } returns flowOf(mockPreferences)
        coEvery { dataStore.edit(any()) } returns mockPreferences

        // Test
        val (backward, forward) = repository.getPlayerButtonsValues()

        // Vérification
        assertEquals(5, backward)
        assertEquals(10, forward)
    }

    @Test
    fun `test setSubtitlesLanguage`() = testScope.runTest {
        // Préparation
        val mockPreferences = preferencesOf()
        coEvery { dataStore.edit(any()) } returns mockPreferences

        // Test
        repository.setSubtitlesLanguage(Locale.FRENCH)

        // Vérification
        coVerify {
            dataStore.edit(match {
                it.toString().contains("fr")
            })
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

}