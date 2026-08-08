package com.mskd.flux.features.catalog.domain.usecase.syncGenres

import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository

class SyncGenresUseCase(
    private val api: ApiRepository,
    private val detailsRepository: DetailsRepository
) {

    suspend operator fun invoke() {

        val newGenres = api.getGenres()
        detailsRepository.saveGenres(newGenres)

    }

}