package com.kaem.flux.di

import com.google.gson.Gson
import com.kaem.flux.BuildConfig
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

    @Provides
    @Singleton
    fun provideHttpClient() : OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor {
                val requestBuilder = it.request().newBuilder()
                requestBuilder.addHeader("accept", "application/json")
                requestBuilder.addHeader("Authorization", "Bearer ${BuildConfig.TMDB_TOKEN}")
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