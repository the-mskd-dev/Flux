package com.mskd.flux.data.tmdb

import com.mskd.flux.model.tmdb.TMDBArtworksResult
import com.mskd.flux.model.tmdb.TMDBAuthentication
import com.mskd.flux.model.tmdb.TMDBEpisode
import com.mskd.flux.model.tmdb.TMDBMovie
import com.mskd.flux.model.tmdb.TMDBSeason
import com.mskd.flux.model.tmdb.TMDBTranslations
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class TMDBServiceImpl(private val client: HttpClient) : TMDBService {

    override suspend fun authenticate(): TMDBAuthentication = client
        .get("authentication")
        .body()

    override suspend fun getMovie(
        title: String,
        year: Int?,
        language: String
    ): TMDBArtworksResult = client
        .get("search/movie") {
            parameter("query", title)
            year?.let { parameter("year", it) }
            parameter("language", language)
        }.body()


    override suspend fun getMovieDetails(
        id: Long,
        language: String
    ): TMDBMovie = client
        .get("movie/$id") {
            parameter("language", language)
        }.body()

    override suspend fun getMovieTranslations(artworkId: Long): TMDBTranslations = client
        .get("movie/$artworkId/translations")
        .body()

    override suspend fun getShow(
        title: String,
        year: Int?,
        language: String
    ): TMDBArtworksResult = client
    .get("search/tv") {
        parameter("query", title)
        year?.let { parameter("year", it) }
        parameter("language", language)
    }.body()

    override suspend fun getShowTranslations(artworkId: Long): TMDBTranslations = client
        .get("tv/$artworkId/translations")
        .body()

    override suspend fun getEpisode(
        id: Long,
        season: Int,
        number: Int,
        language: String
    ): TMDBEpisode = client
        .get("tv/$id/season/$season/episode/$number") {
            parameter("language", language)
        }.body()

    override suspend fun getEpisodeTranslations(
        artworkId: Long,
        season: Int,
        number: Int
    ): TMDBTranslations = client
        .get("tv/$artworkId/season/$season/episode/$number/translations")
        .body()

    override suspend fun getSeason(
        id: Long,
        season: Int,
        language: String
    ): TMDBSeason = client
        .get("tv/$id/season/$season") {
            parameter("language", language)
        }.body()

    override suspend fun getSeasonTranslations(
        artworkId: Long,
        season: Int
    ): TMDBTranslations = client
        .get("tv/$artworkId/season/$season/translations")
        .body()

}