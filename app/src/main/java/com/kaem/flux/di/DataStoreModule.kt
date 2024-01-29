package com.kaem.flux.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

// At the top level of your kotlin file:
val Context.homeDataStore: DataStore<Preferences> by preferencesDataStore(name = "home")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class HomeDataStore

    @Provides
    @Singleton
    @HomeDataStore
    fun provideHomeDataStore(@ApplicationContext context: Context) : DataStore<Preferences> {
        return context.homeDataStore
    }

}