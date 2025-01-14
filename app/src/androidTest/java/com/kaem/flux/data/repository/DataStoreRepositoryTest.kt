package com.kaem.flux.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.filters.SmallTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@SmallTest
class DataStoreRepositoryTest {


    @Test
    fun firstTest() = runTest {
        assert(true)
    }

}