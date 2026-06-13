package com.mskd.flux.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coroutineModule = module {

    single<CoroutineDispatcher>(named("DefaultDispatcher")) {
        Dispatchers.Default
    }

    single<CoroutineScope>(named("ApplicationScope")) {
        val defaultDispatcher = get<CoroutineDispatcher>(named("DefaultDispatcher"))

        CoroutineScope(SupervisorJob() + defaultDispatcher)
    }

}