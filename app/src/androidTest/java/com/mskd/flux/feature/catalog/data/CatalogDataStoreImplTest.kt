package com.mskd.flux.feature.catalog.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.features.catalog.data.datastore.CatalogDataStoreImpl
import com.mskd.flux.features.catalog.domain.datastore.CatalogDataStore
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.domain.model.CatalogViewMode
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

@OptIn(ExperimentalCoroutinesApi::class)
@MediumTest
class CatalogDataStoreImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var catalogDataStore: CatalogDataStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context.preferencesDataStoreFile("TEST_DATASTORE_NAME")

        val testDataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                tempFolder.newFile("test_datastore_${System.nanoTime()}.preferences_pb")
            }
        )

        catalogDataStore = CatalogDataStoreImpl(
            catalogDataStore = testDataStore
        )

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun get_and_set_sorting_mode() = runTest {

        catalogDataStore.flow.test {
            var state = awaitItem()
            assert(state.sortingMode == CatalogSortingMode.LAST_MODIFICATION)

            val newSortingMode = CatalogSortingMode.A_TO_Z
            catalogDataStore.setSortingMode(newSortingMode)
            state = awaitItem()
            assert(state.sortingMode == newSortingMode)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun get_and_set_view_mode() = runTest {

        catalogDataStore.flow.test {
            var state = awaitItem()
            assert(state.viewMode == CatalogViewMode.BY_TYPE)

            val newViewMode = CatalogViewMode.GRID
            catalogDataStore.setViewMode(newViewMode)
            state = awaitItem()
            assert(state.viewMode == newViewMode)

            cancelAndConsumeRemainingEvents()
        }

    }

}