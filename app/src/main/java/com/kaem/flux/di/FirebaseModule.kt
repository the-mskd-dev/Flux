package com.kaem.flux.di

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.remoteConfig
import com.google.gson.Gson
import com.kaem.flux.data.repository.firebase.FirebaseRepository
import com.kaem.flux.data.repository.firebase.FirebaseRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context) : FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseRepository(gson: Gson) : FirebaseRepository {
        val remoteConfig = Firebase.remoteConfig
        return FirebaseRepositoryImpl(remoteConfig = remoteConfig, gson = gson)
    }

}