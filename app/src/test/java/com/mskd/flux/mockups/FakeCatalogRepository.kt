package com.mskd.flux.mockups

import com.mskd.flux.data.repository.catalog.CatalogRepository
import com.mskd.flux.model.UserFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FakeCatalogRepository(
    initialState: CatalogRepository.State = CatalogRepository.State()
) : CatalogRepository {

    private val _flow = MutableStateFlow(initialState)

    override val flow: StateFlow<CatalogRepository.State> = _flow

    override fun syncCatalog() {

        _flow.update { it.copy(isLoading = true) }

        _flow.update {
            it.copy(
                isLoading = false,
                artworks = MediaMockups.artworks
            )
        }

    }

    override suspend fun getFiles(): List<UserFile> {
        return (MediaMockups.movies + MediaMockups.episodes).map { it.file }
    }

}