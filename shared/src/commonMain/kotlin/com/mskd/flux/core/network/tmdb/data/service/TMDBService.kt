package com.mskd.flux.core.network.tmdb.data.service

import com.mskd.flux.core.network.tmdb.data.dto.AuthenticationDto
import com.mskd.flux.core.network.tmdb.data.dto.SearchResultsDto
import com.mskd.flux.core.network.tmdb.data.dto.TranslationsDto
import com.mskd.flux.core.network.tmdb.data.dto.genre.GenresResultDto
import com.mskd.flux.core.network.tmdb.data.dto.movie.MovieDto
import com.mskd.flux.core.network.tmdb.data.dto.show.EpisodeDto
import com.mskd.flux.core.network.tmdb.data.dto.show.SeasonDto
import com.mskd.flux.core.network.tmdb.data.dto.show.ShowDto
import com.mskd.flux.utils.Constants

interface TMDBService {

    //region Auth

    suspend fun authenticate() : AuthenticationDto

    //endregion

    //region Movie

    suspend fun searchMovie(
        title: String,
        year: Int? = null,
        language: String = Constants.Global.LANGUAGE
    ) : SearchResultsDto

    suspend fun getMovieDetails(
        id: Long,
        language: String = Constants.Global.LANGUAGE
    ) : MovieDto

    suspend fun getMovieTranslations(
        artworkId: Long,
    ) : TranslationsDto

    suspend fun getMovieGenres(language: String = Constants.Global.LANGUAGE) : GenresResultDto

    //endregion

    //region Show

    suspend fun searchShow(
        title: String,
        year: Int? = null,
        language: String = Constants.Global.LANGUAGE
    ) : SearchResultsDto

    suspend fun getShowDetails(
        artworkId: Long,
        language: String = Constants.Global.LANGUAGE
    ) : ShowDto

    suspend fun getShowTranslations(
        artworkId: Long,
    ) : TranslationsDto

    suspend fun getShowGenres(language: String = Constants.Global.LANGUAGE) : GenresResultDto

    //endregion

    //region Episode

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

    //endregion

    //region Season

    suspend fun getSeason(
        id: Long,
        season: Int,
        language: String = Constants.Global.LANGUAGE
    ) : SeasonDto

    suspend fun getSeasonTranslations(
        artworkId: Long,
        season: Int,
    ) : TranslationsDto

    //endregion

}