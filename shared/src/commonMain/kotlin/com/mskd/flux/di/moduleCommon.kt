package com.mskd.flux.di

import com.mskd.flux.core.moduleCore
import com.mskd.flux.features.moduleFeatures
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val moduleCommon = module {

    single<CoroutineDispatcher>(Qualifiers.DEFAULT_DISPATCHER) {
        Dispatchers.Default
    }

    single<CoroutineScope>(Qualifiers.APPLICATION_SCOPE) {
        val defaultDispatcher = get<CoroutineDispatcher>(Qualifiers.DEFAULT_DISPATCHER)

        CoroutineScope(SupervisorJob() + defaultDispatcher)
    }

    includes(
        moduleCore,
        moduleFeatures,
    )

}