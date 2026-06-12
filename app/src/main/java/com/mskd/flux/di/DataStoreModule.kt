package com.mskd.flux.di

import android.content.Context
import com.mskd.flux.data.repository.customization.CustomizationRepository
import com.mskd.flux.data.repository.customization.CustomizationRepositoryImpl
import com.mskd.flux.data.repository.customization.customizationDatastore
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
import com.mskd.flux.data.tmdb.token.TokenRepositoryImpl
import com.mskd.flux.data.tmdb.token.tokenDatastore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
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
        json: Json
    ) : UserRepository {
        return UserRepositoryImpl(
            userDataStore = context.userDataStore,
            json = json
        )
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context) : SettingsRepository {
        return SettingsRepositoryImpl(settingsDataStore = context.settingsDatastore)
    }

    @Provides
    @Singleton
    fun provideCustomizationRepository(@ApplicationContext context: Context) : CustomizationRepository {
        return CustomizationRepositoryImpl(customizationDataStore = context.customizationDatastore)
    }

    @Provides
    @Singleton
    fun provideTokenRepository(@ApplicationContext context: Context) : TokenRepository {
        return TokenRepositoryImpl(tokenDataStore = context.tokenDatastore)
    }

    @Provides
    @Singleton
    fun provideSnackbarRepository(@ApplicationContext context: Context) : SnackbarRepository {
        return SnackbarRepositoryImpl(snackbarDataStore = context.snackbarDataStore)
    }

}