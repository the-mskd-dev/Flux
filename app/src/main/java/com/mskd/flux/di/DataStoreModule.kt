package com.mskd.flux.di

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
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// At the top level of your kotlin file:
private const val PREFERENCES = "preferences"

val dataStoreModule = module {

    single<UserRepository> {
        UserRepositoryImpl(
            userDataStore = androidContext().userDataStore,
            json = get()
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            settingsDataStore = androidContext().settingsDatastore
        )
    }

    single<CustomizationRepository> {
        CustomizationRepositoryImpl(
            customizationDataStore = androidContext().customizationDatastore
        )
    }

    single<TokenRepository> {
        TokenRepositoryImpl(
            tokenDataStore = androidContext().tokenDatastore
        )
    }

    single<SnackbarRepository> {
        SnackbarRepositoryImpl(
            snackbarDataStore = androidContext().snackbarDataStore
        )
    }


}