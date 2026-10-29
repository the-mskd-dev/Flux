package com.mskd.flux.features.sources

import com.mskd.flux.features.files.MEDIASTORE_SOURCES
import com.mskd.flux.features.files.SAF_SOURCES
import com.mskd.flux.features.sources.data.AndroidUserFolderValidator
import com.mskd.flux.features.sources.data.provider.AndroidSourcesProvider
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import com.mskd.flux.features.sources.domain.validator.UserFolderValidator
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val moduleSourcesAndroid = module {

    single<UserFolderValidator> {
        AndroidUserFolderValidator(
            context = get()
        )
    }

    single<SourcesProvider> {
        AndroidSourcesProvider(
            settingsDataStore = get(),
            sourcesRepository = get(),
            mediaStoreSource = get(MEDIASTORE_SOURCES),
            safSourceFor = { folder: UserFolder -> get(SAF_SOURCES) { parametersOf(folder) } }
        )
    }

}