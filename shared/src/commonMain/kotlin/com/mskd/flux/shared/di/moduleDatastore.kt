package com.mskd.flux.shared.di

import com.mskd.flux.shared.data.repository.customization.CustomizationRepository
import com.mskd.flux.shared.data.repository.customization.CustomizationRepositoryImpl
import com.mskd.flux.shared.data.repository.settings.SettingsRepository
import com.mskd.flux.shared.data.repository.settings.SettingsRepositoryImpl
import com.mskd.flux.shared.data.repository.snackbars.SnackbarRepository
import com.mskd.flux.shared.data.repository.snackbars.SnackbarRepositoryImpl
import com.mskd.flux.shared.data.repository.token.TokenRepository
import com.mskd.flux.shared.data.repository.token.TokenRepositoryImpl
import com.mskd.flux.shared.data.repository.user.UserRepository
import com.mskd.flux.shared.data.repository.user.UserRepositoryImpl
import org.koin.dsl.module

val moduleDatastore = module {

    single<CustomizationRepository> {
        CustomizationRepositoryImpl(
            customizationDataStore = get(Qualifiers.CUSTOMIZATION_DATASTORE)
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            settingsDataStore = get(Qualifiers.SETTINGS_DATASTORE),
        )
    }

    single<SnackbarRepository> {
        SnackbarRepositoryImpl(
            snackbarDataStore = get(Qualifiers.SNACKBAR_DATASTORE),
        )
    }

    single<TokenRepository> {
        TokenRepositoryImpl(
            tokenDataStore = get(Qualifiers.TOKEN_DATASTORE)
        )
    }

    single<UserRepository> {
        UserRepositoryImpl(
            userDataStore = get(Qualifiers.USER_DATASTORE),
            json = get()
        )
    }

}