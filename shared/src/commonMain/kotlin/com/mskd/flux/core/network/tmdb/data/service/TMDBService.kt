package com.mskd.flux.core.network.tmdb.data.service

import com.mskd.flux.core.network.tmdb.data.dto.ArtworksResultDto
import com.mskd.flux.core.network.tmdb.data.dto.AuthenticationDto
import com.mskd.flux.core.network.tmdb.data.dto.EpisodeDto
import com.mskd.flux.core.network.tmdb.data.dto.MovieDto
import com.mskd.flux.core.network.tmdb.data.dto.SeasonDto
import com.mskd.flux.core.network.tmdb.data.dto.TranslationsDto
import com.mskd.flux.utils.Constants

interface TMDBService {

    suspend fun authenticate() : AuthenticationDto

    suspend fun getMovie(
        title: String,
        year: Int? = null,
        language: String = Constants.Global.LANGUAGE
    ) : ArtworksResultDto

    suspend fun getMovieDetails(
        id: Long,
        language: String = Constants.Global.LANGUAGE
    ) : MovieDto

    suspend fun getMovieTranslations(
        artworkId: Long,
    ) : TranslationsDto

    suspend fun getShow(
        title: String,
        year: Int? = null,
        language: String = Constants.Global.LANGUAGE
    ) : ArtworksResultDto

    suspend fun getShowTranslations(
        artworkId: Long,
    ) : TranslationsDto

    suspend fun getEpisode(
        id: Long,
        season: Int,
        number: Int,
        language: String = Constants.Global.LANGUAGE
    ) : EpisodeDto

    suspend fun getEpisodeTranslations(
        artworkId: Long,
        season: Int,
        number: Int,
    ) : TranslationsDto

    suspend fun getSeason(
        id: Long,
        season: Int,
        language: String = Constants.Global.LANGUAGE
    ) : SeasonDto

    suspend fun getSeasonTranslations(
        artworkId: Long,
        season: Int,
    ) : TranslationsDto

}