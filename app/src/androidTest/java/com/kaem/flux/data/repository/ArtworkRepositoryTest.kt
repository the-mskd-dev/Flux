package com.kaem.flux.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import com.kaem.flux.data.ddb.FluxDatabase
import org.junit.Before

@MediumTest
class ArtworkRepositoryTest {

    private lateinit var repository: ArtworkRepository


    @Before
    fun setUpDatabase() {

        val database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FluxDatabase::class.java
        ).build()

        val db = database.fluxDao()

        repository = ArtworkRepository(db)

    }

}