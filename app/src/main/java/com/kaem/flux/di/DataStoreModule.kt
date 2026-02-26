package com.kaem.flux.di

import android.content.Context
import com.google.gson.Gson
import com.kaem.flux.data.repository.settings.SettingsRepository
import com.kaem.flux.data.repository.user.UserRepository
import com.kaem.flux.data.repository.settings.SettingsRepositoryImpl
import com.kaem.flux.data.repository.settings.settingsDatastore
import com.kaem.flux.data.repository.user.UserRepositoryImpl
import com.kaem.flux.data.repository.user.userDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// At the top level of your kotlin file:
private const val PREFERENCES = "preferences"

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context,
        gson: Gson
    ) : UserRepository {
        return UserRepositoryImpl(
            userDataStore = context.userDataStore,
            gson = gson
        )
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context) : SettingsRepository {
        return SettingsRepositoryImpl(settingsDataStore = context.settingsDatastore)
    }

}