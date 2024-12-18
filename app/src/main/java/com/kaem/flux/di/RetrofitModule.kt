package com.kaem.flux.di

import com.google.gson.Gson
import com.kaem.flux.data.tmdb.TMDBService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val TMDB_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyYWUwM2ExNmE5NjQ5NmJlZjdiMzI5OTZhZWIzYWMzOSIsIm5iZiI6MTcwNTkzMTMyMi4xMDUsInN1YiI6IjY1YWU3MjNhODQ4ZWI5MDBhYzljNDQ0ZCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.c0I07zJ-DWdveIZoUd_GrBSaKKU3pUYolOCOIrgGwVg"

    @Provides
    @Singleton
    fun provideHttpClient() : OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor {
                val requestBuilder = it.request().newBuilder()
                requestBuilder.addHeader("accept", "application/json")
                requestBuilder.addHeader("Authorization", "Bearer $TMDB_TOKEN")
                it.proceed(requestBuilder.build())
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        gson: Gson,
        okHttpClient: OkHttpClient
    ) : Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideTMDBService(retrofit: Retrofit) : TMDBService {
        return retrofit.create(TMDBService::class.java)
    }

}