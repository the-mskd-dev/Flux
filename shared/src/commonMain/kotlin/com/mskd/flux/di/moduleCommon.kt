package com.mskd.flux.di

import com.mskd.flux.features.moduleFeatures
import org.koin.dsl.module

val moduleCommon = module {

    includes(
        moduleCoroutine,
        moduleDatabase,
        moduleDatastore,
        moduleImages,
        moduleUseCase,
        moduleViewModel,
        moduleFeatures
    )

}