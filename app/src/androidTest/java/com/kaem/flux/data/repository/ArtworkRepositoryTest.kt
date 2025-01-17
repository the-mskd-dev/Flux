package com.kaem.flux.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.ddb.FluxDatabase
import org.junit.After
import org.junit.Before

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

}