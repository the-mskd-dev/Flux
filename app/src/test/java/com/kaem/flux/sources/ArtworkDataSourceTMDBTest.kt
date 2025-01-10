package com.kaem.flux.sources

import com.kaem.flux.ApiTest
import com.kaem.flux.data.source.artwork.ArtworkDataSourceTMDBImpl
import com.kaem.flux.mockups.FilesMockups
import org.junit.Before
import org.junit.Test

class ArtworkDataSourceTMDBTest : ApiTest() {

    private lateinit var dataSource: ArtworkDataSourceTMDBImpl

    @Before
    override fun setUp() {
        super.setUp()

        dataSource = ArtworkDataSourceTMDBImpl(api)

    }

    @Test
    fun create_user_folders() {

        val files = FilesMockups.localFiles

        val folders = dataSource.createFolders(files)

        assert(folders.size == 3)

        val narutoFolder = folders.firstOrNull { it.title == "naruto" }

        assert(narutoFolder?.files?.size == 4)
    }

}