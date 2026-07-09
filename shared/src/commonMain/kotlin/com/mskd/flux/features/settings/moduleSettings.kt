package com.mskd.flux.features.settings

import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.settings.data.datastore.SettingsDataStoreImpl
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleSettings = module {

    viewModelOf(::SettingsViewModel)

    single<SettingsDataStore> {
        SettingsDataStoreImpl(
            settingsDataStore = get(Qualifiers.SETTINGS_DATASTORE),
        )
    }

}