package com.mskd.flux.features.welcome

import com.mskd.flux.features.welcome.presentation.WelcomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleWelcome = module {

    viewModelOf(::WelcomeViewModel)

}