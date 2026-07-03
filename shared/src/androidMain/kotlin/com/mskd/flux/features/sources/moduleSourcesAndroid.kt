package com.mskd.flux.features.sources

import com.mskd.flux.features.sources.data.AndroidUserFolderValidator
import com.mskd.flux.features.sources.domain.validator.UserFolderValidator
import org.koin.dsl.module

val moduleSourcesAndroid = module {

    single<UserFolderValidator> {
        AndroidUserFolderValidator(
            context = get()
        )
    }

}