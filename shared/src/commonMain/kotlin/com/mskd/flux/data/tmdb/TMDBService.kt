package com.mskd.flux.data.tmdb

import com.mskd.flux.model.tmdb.TMDBArtworksResult
import com.mskd.flux.model.tmdb.TMDBAuthentication
import com.mskd.flux.model.tmdb.TMDBEpisode
import com.mskd.flux.model.tmdb.TMDBMovie
import com.mskd.flux.model.tmdb.TMDBSeason
import com.mskd.flux.model.tmdb.TMDBTranslations
import com.mskd.flux.utils.Constants

interface TMDBService {

    suspend fun authenticate() : TMDBAuthentication

    suspend fun getMovie(
        title: String,
        year: Int? = null,
        language: String = Constants.Global.LANGUAGE
    ) : TMDBArtworksResult

    suspend fun getMovieDetails(
        id: Long,
        language: String = Constants.Global.LANGUAGE
    ) : TMDBMovie

    suspend fun getMovieTranslations(
        artworkId: Long,
    ) : TMDBTranslations

    suspend fun getShow(
        title: String,
        year: Int? = null,
        language: String = Constants.Global.LANGUAGE
    ) : TMDBArtworksResult

    suspend fun getShowTranslations(
        artworkId: Long,
    ) : TMDBTranslations

    suspend fun getEpisode(
        id: Long,
        season: Int,
        number: Int,
        language: String = Constants.Global.LANGUAGE
    ) : TMDBEpisode

    suspend fun getEpisodeTranslations(
        artworkId: Long,
        season: Int,
        number: Int,
    ) : TMDBTranslations

    suspend fun getSeason(
        id: Long,
        season: Int,
        language: String = Constants.Global.LANGUAGE
    ) : TMDBSeason

    suspend fun getSeasonTranslations(
        artworkId: Long,
        season: Int,
    ) : TMDBTranslations

}