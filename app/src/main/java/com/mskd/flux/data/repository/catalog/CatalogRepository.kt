package com.mskd.flux.data.repository.catalog

import com.mskd.flux.model.UserFile
import com.mskd.flux.model.artwork.Artwork
import kotlinx.coroutines.flow.StateFlow


interface CatalogRepository {

    val flow: StateFlow<State>

    suspend fun syncCatalog()

    suspend fun getFiles() : List<UserFile>

    data class State(
        val isLoading: Boolean = true,
        val artworks: List<Artwork> = emptyList()
    )

}