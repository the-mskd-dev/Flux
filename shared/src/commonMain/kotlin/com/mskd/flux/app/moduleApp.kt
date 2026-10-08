package com.mskd.flux.app

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleApp = module {

    viewModelOf(::AppViewModel)

}