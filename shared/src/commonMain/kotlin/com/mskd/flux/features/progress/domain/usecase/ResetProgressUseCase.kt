package com.mskd.flux.features.progress.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Status

class ResetProgressUseCase(
    private val database: DatabaseRepository,
    private val user: UserDataStore
) {

    companion object {
        const val TAG = "ResetProgressUseCase"
    }

    suspend operator fun invoke(artwork: Artwork, season: Int?) {
        when (artwork.type) {
            ContentType.SHOW -> {

                val episodes = database.getEpisodes(artworkId = artwork.id)
                val updatedEpisodes = episodes
                    .filter { if (season != null) it.season == season else true }
                    .filter { it.status != Status.TO_WATCH || it.currentTime != 0L }
                    .map { it.copy(currentTime = 0L, status = Status.TO_WATCH) }

                database.saveMedias(medias = updatedEpisodes)

            }
            ContentType.MOVIE -> {

                database.getMovie(artworkId = artwork.id)?.let { movie ->
                    val updatedMovie = movie.copy(currentTime = 0L, status = Status.TO_WATCH)

                    database.saveMedias(listOf(updatedMovie))
                }

            }
        }

        user.removeFromRecentlyWatched(artworkId = artwork.id)

    }
}