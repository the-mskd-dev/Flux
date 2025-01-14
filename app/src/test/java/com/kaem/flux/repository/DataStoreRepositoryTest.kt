package com.kaem.flux.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import app.cash.turbine.test
import com.google.gson.Gson
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.ui.theme.Ui
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.util.Locale

class DataStoreRepositoryTest : BaseTest() {

    private lateinit var dataStoreRepository: DataStoreRepository
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var gson: Gson

    override fun setUp() {
        super.setUp()

        dataStore = mockk(relaxed = true)
        gson = Gson()
        dataStoreRepository = DataStoreRepository(dataStore, gson)

    }

    @Test
    fun `initial state`() = runTest {
        // Préparation des données
        val mockPreferences = preferencesOf(
            DataStoreRepository.Keys.LAST_WATCHED_IDS to "[1, 2, 3]",
            DataStoreRepository.Keys.PLAYER_BACKWARD to "15",
            DataStoreRepository.Keys.PLAYER_FORWARD to "30",
            DataStoreRepository.Keys.UI_THEME to "DARK",
            DataStoreRepository.Keys.SUBTITLES_LANGUAGE to "fr"
        )

        every { dataStore.data } returns flowOf(mockPreferences)

        // Collecte la première valeur
        val initialState = dataStoreRepository.flow.first()

        // Vérifications
        assert(initialState.lastWatchedIds.isNotEmpty())
        Assert.assertEquals(listOf(1L, 2L, 3L), initialState.lastWatchedIds)
        Assert.assertEquals(15, initialState.playerBackwardValue)
        Assert.assertEquals(30, initialState.playerForwardValue)
        Assert.assertEquals(Ui.THEME.DARK, initialState.uiTheme)
        Assert.assertEquals(Locale.FRENCH, initialState.subtitlesLanguage)
    }


}