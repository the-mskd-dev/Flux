package com.kaem.flux.data.tmdb

import com.google.gson.GsonBuilder
import com.kaem.flux.model.tmdb.TMDBAuthentication
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface TMDBService {

    @GET("authentication")
    fun authenticate() : TMDBAuthentication

}

object TMDBClient {

    private const val BASE_URL = "https://api.themoviedb.org/3/"

    private val gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    private val retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    }

    val service: TMDBService by lazy {
        retrofit.create(TMDBService::class.java)
    }

}