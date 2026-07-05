package com.mskd.flux.features.artwork.domain.usecase.saveArtwork

import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.domain.model.artwork.Episode
import com.mskd.flux.core.domain.model.artwork.Movie

internal class SaveArtworkUseCaseImpl(
    private val database: DatabaseRepository
) : SaveArtworkUseCase {

    override suspend fun saveMovie(movie: Movie) {
        database.saveMovies(listOf(movie))
    }

    override suspend fun saveEpisode(episode: Episode) {
        database.saveEpisodes(listOf(episode))
    }

    override suspend fun saveEpisodes(episodes: List<Episode>) {
        database.saveEpisodes(episodes)
    }

}