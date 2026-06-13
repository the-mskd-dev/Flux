package com.mskd.flux.di

import com.mskd.flux.screens.home.HomeViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val viewModelsModule = module {

    singleOf(::HomeViewModel)

}