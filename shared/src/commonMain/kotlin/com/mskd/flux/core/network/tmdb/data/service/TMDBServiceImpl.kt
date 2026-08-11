package com.mskd.flux.core.network.tmdb.data.service

import com.mskd.flux.core.network.tmdb.data.dto.AuthenticationDto
import com.mskd.flux.core.network.tmdb.data.dto.SearchResultsDto
import com.mskd.flux.core.network.tmdb.data.dto.TranslationsDto
import com.mskd.flux.core.network.tmdb.data.dto.genre.GenresResultDto
import com.mskd.flux.core.network.tmdb.data.dto.movie.MovieDto
import com.mskd.flux.core.network.tmdb.data.dto.show.EpisodeDto
import com.mskd.flux.core.network.tmdb.data.dto.show.SeasonDto
import com.mskd.flux.core.network.tmdb.data.dto.show.ShowDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class TMDBServiceImpl(private val client: HttpClient) : TMDBService {

    //region Auth

    override suspend fun authenticate(): AuthenticationDto = client
        .get("authentication")
        .body()

    //endregion

    //region Movie

    override suspend fun searchMovie(
        title: String,
        year: Int?,
        language: String
    ): SearchResultsDto = client
        .get("search/movie") {
            parameter("query", title)
            year?.let { parameter("year", it) }
            parameter("language", language)
        }.body()


    override suspend fun getMovieDetails(
        id: Long,
        language: String
    ): MovieDto = client
        .get("movie/$id") {
            parameter("language", language)
        }.body()

    override suspend fun getMovieTranslations(artworkId: Long): TranslationsDto = client
        .get("movie/$artworkId/translations")
        .body()

    override suspend fun getMovieGenres(language: String): GenresResultDto = client
        .get("genre/movie/list") {
            parameter("language", language)
        }.body()

    //endregion

    //region Show

    override suspend fun searchShow(
        title: String,
        year: Int?,
        language: String
    ): SearchResultsDto = client
    .get("search/tv") {
        parameter("query", title)
        year?.let { parameter("year", it) }
        parameter("language", language)
    }.body()

    override suspend fun getShowDetails(
        artworkId: Long,
        language: String
    ): ShowDto = client
        .get("tv/$artworkId") {
            parameter("language", language)
        }.body()

    override suspend fun getShowTranslations(artworkId: Long): TranslationsDto = client
        .get("tv/$artworkId/translations")
        .body()

    override suspend fun getShowGenres(language: String): GenresResultDto = client
        .get("genre/tv/list") {
            parameter("language", language)
        }.body()

    //endregion

    //region Episode

    override suspend fun getEpisode(
        id: Long,
        season: Int,
        number: Int,
        language: String
    ): EpisodeDto = client
        .get("tv/$id/season/$season/episode/$number") {
            parameter("language", language)
        }.body()

    override suspend fun getEpisodeTranslations(
        artworkId: Long,
        season: Int,
        number: Int
    ): TranslationsDto = client
        .get("tv/$artworkId/season/$season/episode/$number/translations")
        .body()

    //endregion

    //region Season

    override suspend fun getSeason(
        id: Long,
        season: Int,
        language: String
    ): SeasonDto = client
        .get("tv/$id/season/$season") {
            parameter("language", language)
        }.body()

    override suspend fun getSeasonTranslations(
        artworkId: Long,
        season: Int
    ): TranslationsDto = client
        .get("tv/$artworkId/season/$season/translations")
        .body()

    //endregion

}