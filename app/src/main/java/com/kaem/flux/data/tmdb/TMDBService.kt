package com.kaem.flux.data.tmdb

import com.google.gson.GsonBuilder
import com.kaem.flux.model.tmdb.TMDBAuthentication
import com.kaem.flux.model.tmdb.TMDBEpisode
import com.kaem.flux.model.tmdb.TMDBArtworksResult
import com.kaem.flux.model.tmdb.TMDBMovie
import com.kaem.flux.utils.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBService {

    @GET("authentication")
    suspend fun authenticate() : TMDBAuthentication

    @GET("search/multi")
    suspend fun getArtworks(
        @Query("query") name: String,
        @Query("language") language: String = Constants.Global.LANGUAGE
    ) : TMDBArtworksResult

    @GET("tv/{id}/season/{season}/episode/{episode}")
    suspend fun getEpisode(
        @Path("id") seriesId: Int,
        @Path("season") season: Int,
        @Path("episode") episode: Int,
        @Query("language") language: String = Constants.Global.LANGUAGE
    ) : TMDBEpisode

    @GET("movie/{id}")
    suspend fun getMovie(
        @Path("id") movieId: Int,
        @Query("language") language: String = Constants.Global.LANGUAGE
    ) : TMDBMovie

}