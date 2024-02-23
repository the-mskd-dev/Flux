package com.kaem.flux.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kaem.flux.model.flux.ArtworkContent
import com.kaem.flux.typeAdapters.ArtworkContentTypeAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GsonModule {

    @Provides
    @Singleton
    fun provideGson() : Gson {
        return GsonBuilder()
            .registerTypeAdapter(ArtworkContent::class.java, ArtworkContentTypeAdapter())
            .setLenient()
            .create()
    }

}