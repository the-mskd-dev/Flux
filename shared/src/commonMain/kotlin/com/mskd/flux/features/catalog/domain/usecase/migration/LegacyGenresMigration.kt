package com.mskd.flux.features.catalog.domain.usecase.migration

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.SyncGenresUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class LegacyGenresMigration(
    private val database: DatabaseRepository,
    private val detailsRepository: DetailsRepository,
    private val api: ApiRepository,
    private val syncGenresUseCase: SyncGenresUseCase,
) {

    suspend fun getSteps(): Int {
        val artworksToUpdate = database.getArtworks().count { it.genreIds.isEmpty() && !it.isUnknown }
        val genresUpdateIsNeeded = if (detailsRepository.getGenresCount() == 0) 1 else 0

        return artworksToUpdate + genresUpdateIsNeeded
    }

    suspend fun migrate(onProgress: () -> Unit) {

        // Get all genres
        if (detailsRepository.getGenresCount() == 0) {
            syncGenresUseCase()
            onProgress()
        }

        val artworks = database.getArtworks().filter { it.genreIds.isEmpty() && !it.isUnknown }

        val updatedArtworks = coroutineScope {

            artworks.map { artwork ->

                async {

                    val genresIds = api.getGenreIds(artwork = artwork).toImmutableList()
                    artwork.copy(genreIds = genresIds).also {
                        onProgress()
                    }

                }


            }.awaitAll()

        }

        database.saveArtworks(artworks = updatedArtworks, overrideLastModification = false)

    }

}