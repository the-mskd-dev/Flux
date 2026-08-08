package com.mskd.flux.features.catalog.domain.usecase.syncGenres

import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.network.tmdb.domain.repository.ArtworkRemoteRepository

class SyncGenresUseCase(
    private val remoteRepository: ArtworkRemoteRepository,
    private val detailsRepository: DetailsRepository
) {

    suspend operator fun invoke() {

        val newGenres = remoteRepository.getGenres()
        detailsRepository.saveGenres(newGenres)

    }

}