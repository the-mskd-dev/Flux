package com.mskd.flux.di

import com.mskd.flux.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleAndroidApp = module {

    viewModelOf(::MainViewModel)

}