package com.mskd.flux.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.core.datastore.data.UserDataStoreImpl
import com.mskd.flux.core.datastore.domain.UserDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
@MediumTest
class UserDataStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userDataStore: UserDataStore
    private val json = Json

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context.preferencesDataStoreFile("TEST_DATASTORE_NAME")

        val testDataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                tempFolder.newFile("test_datastore_${System.nanoTime()}.preferences_pb")
            }
        )

        userDataStore = UserDataStoreImpl(
            userDataStore = testDataStore,
            json = json
        )

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state() = runTest {

        val defaultPreferences = UserDataStore.State()

        userDataStore.flow.test {

            val initialState = awaitItem()

            assert(defaultPreferences.recentlyWatchedIds == initialState.recentlyWatchedIds)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun add_and_remove_watched_media_id() = runTest {

        val idTest = 4L

        userDataStore.flow.test {

            var state = awaitItem()
            assert(state.recentlyWatchedIds.isEmpty())

            userDataStore.addToRecentlyWatched(idTest)
            state = awaitItem()
            assert(state.recentlyWatchedIds.contains(idTest))

            userDataStore.removeFromRecentlyWatched(idTest)
            state = awaitItem()
            assert(!state.recentlyWatchedIds.contains(idTest))

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_sync_time() = runTest {

        var syncTime = userDataStore.flow.first().syncTime
        assert(syncTime == 0L)

        val testTime = 123456789L
        userDataStore.setSyncTime(testTime)

        syncTime = userDataStore.flow.first().syncTime

        assert(syncTime == testTime)
    }



}