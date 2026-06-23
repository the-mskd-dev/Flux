package com.mskd.flux.di

import org.koin.dsl.module

val moduleCommon = module {

    includes(
        moduleCoroutine,
        moduleDatabase,
        moduleDatastore,
        moduleImages,
        moduleNetwork,
        moduleRepository,
        moduleUseCase,
        moduleViewModel
    )

}