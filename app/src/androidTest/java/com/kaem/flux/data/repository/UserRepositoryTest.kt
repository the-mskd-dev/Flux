package com.kaem.flux.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.google.gson.Gson
import com.kaem.flux.data.repository.user.UserRepository
import com.kaem.flux.data.repository.user.UserRepositoryImpl
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

@OptIn(ExperimentalCoroutinesApi::class)
@MediumTest
class UserRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context.preferencesDataStoreFile("TEST_DATASTORE_NAME")

        val testDataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                tempFolder.newFile("test_datastore_${System.nanoTime()}.preferences_pb")
            }
        )

        userRepository = UserRepositoryImpl(
            userDataStore = testDataStore,
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

        val defaultPreferences = UserRepository.State()

        userRepository.flow.test {

            val initialState = awaitItem()

            assert(defaultPreferences.recentlyWatchedIds == initialState.recentlyWatchedIds)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun add_and_remove_watched_media_id() = runTest {

        val idTest = 4L

        userRepository.flow.test {

            var state = awaitItem()
            assert(state.recentlyWatchedIds.isEmpty())

            userRepository.addToRecentlyWatched(idTest)
            state = awaitItem()
            assert(state.recentlyWatchedIds.contains(idTest))

            userRepository.removeFromRecentlyWatched(idTest)
            state = awaitItem()
            assert(!state.recentlyWatchedIds.contains(idTest))

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_sync_time() = runTest {

        var syncTime = userRepository.flow.first().syncTime
        assert(syncTime == 0L)

        val testTime = 123456789L
        userRepository.setSyncTime(testTime)

        syncTime = userRepository.flow.first().syncTime

        assert(syncTime == testTime)
    }



}