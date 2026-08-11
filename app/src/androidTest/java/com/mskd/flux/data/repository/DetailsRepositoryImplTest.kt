package com.mskd.flux.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.core.database.data.repository.DetailsRepositoryImpl
import com.mskd.flux.mockups.DetailsMockup
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class DetailsRepositoryImplTest {

    //region Setup

    private lateinit var database: FluxDatabase
    private lateinit var repository: DetailsRepositoryImpl

    @Before
    fun setUp() {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                FluxDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()

        repository = DetailsRepositoryImpl(database.detailsDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    //endregion

    //region Genre

    @Test
    fun flowGenres_returns_flow_of_inserted_genres() = runTest {
        // Given
        val genres = DetailsMockup.allGenres
        repository.saveGenres(genres = genres)

        // When/Then
        repository.flowGenres().test {
            val result = awaitItem()
            assertEquals(genres.distinctBy { it.id }.size, result.size)
            assertTrue(result.containsAll(genres))
        }
    }

    @Test
    fun getGenresCount_returns_count_of_inserted_genres() = runTest {
        // Given
        val genres = DetailsMockup.allGenres

        // When
        repository.saveGenres(genres)

        // Then
        val result = repository.getGenresCount()
        assertEquals(genres.size, result)
    }

    //endregion

}