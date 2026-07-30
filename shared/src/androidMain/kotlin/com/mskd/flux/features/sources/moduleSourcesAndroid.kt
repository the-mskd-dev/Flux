package com.mskd.flux.features.sources

import com.mskd.flux.features.files.MEDIASTORE_SOURCES
import com.mskd.flux.features.files.SAF_SOURCES
import com.mskd.flux.features.sources.data.AndroidUserFolderValidator
import com.mskd.flux.features.sources.data.provider.SourcesProviderImpl
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import com.mskd.flux.features.sources.domain.validator.UserFolderValidator
import org.koin.dsl.module

val moduleSourcesAndroid = module {

    single<UserFolderValidator> {
        AndroidUserFolderValidator(
            context = get()
        )
    }

    single<SourcesProvider> {
        SourcesProviderImpl(
            settingsDataStore = get(),
            mediaStoreSource = get(MEDIASTORE_SOURCES),
            safSource = get(SAF_SOURCES)
        )
    }

}