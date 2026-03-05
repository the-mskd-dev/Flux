package com.kaem.flux.data.repository.catalog

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.Artwork
import kotlinx.coroutines.flow.StateFlow


interface CatalogRepository {

    val flow: StateFlow<Content>

    suspend fun getCatalog(sync: Boolean = false)

    suspend fun syncCatalog() : List<Artwork>

    suspend fun getFiles() : List<UserFile>

    data class Content(
        val isLoading: Boolean = true,
        val artworks: List<Artwork> = emptyList()
    )

}