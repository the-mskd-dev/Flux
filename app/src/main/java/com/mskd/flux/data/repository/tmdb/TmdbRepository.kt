package com.mskd.flux.data.repository.tmdb

import com.mskd.flux.model.UserFile
import com.mskd.flux.model.tmdb.TMDBArtwork
import com.mskd.flux.model.tmdb.TMDBEpisode
import com.mskd.flux.model.tmdb.TMDBMovie

interface TmdbRepository {

    suspend fun getTmdbArtwork(file: UserFile) : TMDBArtwork?

    suspend fun getTmdbMovie(artworkId: Long) : TMDBMovie?

    suspend fun getTmdbEpisode(
        artworkId: Long,
        season: Int,
        number: Int,
    ) : TMDBEpisode?

}