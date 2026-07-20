package com.mskd.flux.core.network.tmdb.data.service

import com.mskd.flux.core.network.tmdb.data.dto.ArtworksResultDto
import com.mskd.flux.core.network.tmdb.data.dto.AuthenticationDto
import com.mskd.flux.core.network.tmdb.data.dto.EpisodeDto
import com.mskd.flux.core.network.tmdb.data.dto.MovieDto
import com.mskd.flux.core.network.tmdb.data.dto.SeasonDto
import com.mskd.flux.core.network.tmdb.data.dto.TranslationsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class TMDBServiceImpl(private val client: HttpClient) : TMDBService {

    override suspend fun authenticate(): AuthenticationDto = client
        .get("authentication")
        .body()

    override suspend fun getMovie(
        title: String,
        year: Int?,
        language: String
    ): ArtworksResultDto = client
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

    override suspend fun getShow(
        title: String,
        year: Int?,
        language: String
    ): ArtworksResultDto = client
    .get("search/tv") {
        parameter("query", title)
        year?.let { parameter("year", it) }
        parameter("language", language)
    }.body()

    override suspend fun getShowTranslations(artworkId: Long): TranslationsDto = client
        .get("tv/$artworkId/translations")
        .body()

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

}