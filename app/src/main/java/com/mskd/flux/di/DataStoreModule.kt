package com.mskd.flux.di

import android.content.Context
import com.google.gson.Gson
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.settings.SettingsRepositoryImpl
import com.mskd.flux.data.repository.settings.settingsDatastore
import com.mskd.flux.data.repository.snackbars.SnackbarRepository
import com.mskd.flux.data.repository.snackbars.SnackbarRepositoryImpl
import com.mskd.flux.data.repository.snackbars.snackbarDataStore
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.data.repository.user.UserRepositoryImpl
import com.mskd.flux.data.repository.user.userDataStore
import com.mskd.flux.data.tmdb.token.TokenRepository
import com.mskd.flux.data.tmdb.token.TokenRepositoryImp
import com.mskd.flux.data.tmdb.token.tokenDatastore
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

    @Provides
    @Singleton
    fun provideTokenProvider(@ApplicationContext context: Context) : TokenRepository {
        return TokenRepositoryImp(tokenDataStore = context.tokenDatastore)
    }

    @Provides
    @Singleton
    fun provideSnackbarRepository(@ApplicationContext context: Context) : SnackbarRepository {
        return SnackbarRepositoryImpl(snackbarDataStore = context.snackbarDataStore)
    }

}