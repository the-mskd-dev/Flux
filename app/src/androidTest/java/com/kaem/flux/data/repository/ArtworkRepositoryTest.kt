package com.kaem.flux.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.ddb.FluxDatabase
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@MediumTest
class ArtworkRepositoryTest {

    private lateinit var database: FluxDatabase
    private lateinit var db: FluxDao
    private lateinit var repository: ArtworkRepository


    @Before
    fun setUpDatabase() {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FluxDatabase::class.java
        ).build()

        db = database.fluxDao()

        repository = ArtworkRepository(db)

    }

    @After
    fun tearDown() {
        database.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun get_artwork() = runTest {

        val content = repository.getArtwork(1L)
        advanceUntilIdle()

        coVerify {
            db.getOverview(any())
        }
    }
}