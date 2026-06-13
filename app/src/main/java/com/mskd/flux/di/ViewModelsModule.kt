package com.mskd.flux.di

import com.mskd.flux.MainViewModel
import com.mskd.flux.screens.home.HomeViewModel
import com.mskd.flux.screens.unknown.UnknownViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelsModule = module {

    viewModelOf(::MainViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::UnknownViewModel)

}