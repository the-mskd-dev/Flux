package com.mskd.flux.features.catalog.domain.usecase.syncGenres

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class UpdateGenreIdsUseCase(
    private val api: ApiRepository,
    private val db: DatabaseRepository
) {

    suspend operator fun invoke(onProgressCount: (Int) -> Unit, onProgress: () -> Unit) {

        val artworks = db.getArtworks().filter { it.genreIds.isEmpty() && !it.isUnknown }

        if (artworks.isEmpty())
            return

        onProgressCount(artworks.size)

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

        db.saveArtworks(artworks = updatedArtworks, overrideLastModification = false)

    }

}