package com.mskd.flux.data.repository.tmdb

import com.mskd.flux.model.UserFile
import com.mskd.flux.model.tmdb.TMDBArtwork
import com.mskd.flux.model.tmdb.TMDBEpisode
import com.mskd.flux.model.tmdb.TMDBMovie
import com.mskd.flux.model.tmdb.TMDBSeason
import com.mskd.flux.model.tmdb.TMDBTranslations
import java.util.Locale

interface TmdbRepository {

    suspend fun getTmdbArtwork(file: UserFile) : TMDBArtwork?

    suspend fun getTmdbMovie(artworkId: Long) : TMDBMovie?

    suspend fun getTmdbEpisode(
        artworkId: Long,
        season: Int,
        number: Int,
    ) : TMDBEpisode?

    suspend fun getTmdbSeason(
        artworkId: Long,
        season: Int,
    ) : TMDBSeason?

    suspend fun getTmdbTranslations(
        request: TMDBTranslations.Request,
        language: Locale
    ) : TMDBTranslations.Translation?

}