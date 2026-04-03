package com.kaem.flux.mockups

import com.kaem.flux.data.repository.catalog.CatalogRepository
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.Artwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FakeCatalogRepository(
    initialContent: CatalogRepository.Content = CatalogRepository.Content()
) : CatalogRepository {

    private val _flow = MutableStateFlow(initialContent)

    override val flow: StateFlow<CatalogRepository.Content> = _flow

    var lastSyncParam: Boolean? = null

    override suspend fun getCatalog(sync: Boolean) {

        lastSyncParam = sync

        _flow.update { it.copy(isLoading = true) }

        _flow.update {
            it.copy(
                isLoading = false,
                artworks = MediaMockups.artworks
            )
        }

    }

    override suspend fun syncCatalog(): List<Artwork> {
        return MediaMockups.artworks
    }

    override suspend fun getFiles(): List<UserFile> {
        return (MediaMockups.movies + MediaMockups.episodes).map { it.file }
    }

}