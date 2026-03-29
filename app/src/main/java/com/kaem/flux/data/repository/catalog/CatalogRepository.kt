package com.kaem.flux.data.repository.catalog

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.Artwork
import kotlinx.coroutines.flow.StateFlow


interface CatalogRepository {

    val flow: StateFlow<State>

    suspend fun loadCatalog(sync: Boolean = false)

    suspend fun getCatalog() : List<Artwork>

    suspend fun getFiles() : List<UserFile>

    data class State(
        val isLoading: Boolean = true,
        val artworks: List<Artwork> = emptyList()
    )

}