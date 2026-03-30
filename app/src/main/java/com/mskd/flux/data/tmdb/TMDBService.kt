package com.mskd.flux.data.tmdb

import com.mskd.flux.model.tmdb.TMDBArtworksResult
import com.mskd.flux.model.tmdb.TMDBAuthentication
import com.mskd.flux.model.tmdb.TMDBEpisode
import com.mskd.flux.model.tmdb.TMDBMovie
import com.mskd.flux.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBService {

    @GET("authentication")
    suspend fun authenticate() : TMDBAuthentication

    @GET("search/movie")
    suspend fun getMovie(
        @Query("query") title: String,
        @Query("year") year: Int? = null,
        @Query("language") language: String = Constants.Global.LANGUAGE
    ) : TMDBArtworksResult

    @GET("movie/{id}")
    suspend fun getMovieDetails(
        @Path("id") id: Long,
        @Query("language") language: String = Constants.Global.LANGUAGE
    ) : TMDBMovie

    @GET("search/tv")
    suspend fun getShow(
        @Query("query") title: String,
        @Query("year") year: Int? = null,
        @Query("language") language: String = Constants.Global.LANGUAGE
    ) : TMDBArtworksResult

    @GET("tv/{id}/season/{season}/episode/{episode}")
    suspend fun getEpisode(
        @Path("id") id: Long,
        @Path("season") season: Int,
        @Path("episode") episode: Int,
        @Query("language") language: String = Constants.Global.LANGUAGE
    ) : TMDBEpisode

}