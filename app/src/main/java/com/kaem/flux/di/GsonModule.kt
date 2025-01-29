package com.kaem.flux.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness

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
            .setStrictness(Strictness.LENIENT)
            .create()
    }

}