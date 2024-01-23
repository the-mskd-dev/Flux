package com.kaem.flux.data.tmdb

import com.google.gson.GsonBuilder
import com.kaem.flux.model.tmdb.TMDBAuthentication
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers

interface TMDBService {

    @GET("authentication")
    fun authenticate() : TMDBAuthentication

}

object TMDBClient {

    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val TMDB_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyYWUwM2ExNmE5NjQ5NmJlZjdiMzI5OTZhZWIzYWMzOSIsInN1YiI6IjY1YWU3MjNhODQ4ZWI5MDBhYzljNDQ0ZCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.EDyA2TZUKZjLYR8dhkIe0gN1RQuKIuunQjO9WhwFEOQ"

    private val gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addNetworkInterceptor {
                val requestBuilder = it.request().newBuilder()
                requestBuilder.addHeader("Content-Type", "application/json")
                requestBuilder.addHeader("Authorization", "Bearer $TMDB_TOKEN")
                it.proceed(requestBuilder.build())
            }
            .build()
    }

    private val retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    }

    val service: TMDBService by lazy {
        retrofit.create(TMDBService::class.java)
    }

}