package com.kaem.flux.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.google.gson.Gson
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.DataStoreRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DataStoreRepositoryTest : BaseTest() {

    private lateinit var dataStoreRepository: DataStoreRepository
    private lateinit var dataStore: DataStore<Preferences>
    private val gson = Gson()

    override fun setUp() {
        super.setUp()

        /*mockkStatic(DataStore::class)
        mockkStatic(Preferences::class)*/
        dataStore = mockk(relaxed = true)
        val data = mockk<Preferences> {
            every { get(DataStoreRepository.Keys.LAST_WATCHED_IDS) } returns "[]"
            every { get(DataStoreRepository.Keys.PLAYER_BACKWARD) } returns "10"
            every { get(DataStoreRepository.Keys.PLAYER_FORWARD) } returns "10"
            every { get(DataStoreRepository.Keys.UI_THEME) } returns "SYSTEM"
            every { get(DataStoreRepository.Keys.SUBTITLES_LANGUAGE) } returns "en"
        }
        every { dataStore.data } returns flowOf(data)

        dataStoreRepository = DataStoreRepository(dataStore, gson)

    }

    @Test
    fun `initial state`() = runTest {
        dataStoreRepository.flow.test {
            val initialState = awaitItem()

            assert(initialState.lastWatchedIds.isEmpty())
        }
    }


}