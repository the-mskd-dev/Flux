package com.kaem.flux.data.tmdb

import com.kaem.flux.model.tmdb.TMDBAuthentication
import retrofit2.http.GET

interface TMDBService {


    @GET("authentication")
    fun authenticate() : TMDBAuthentication

}