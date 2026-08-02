package com.mskd.flux.features.setup

import com.mskd.flux.features.setup.presentation.SetupViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleSetup = module {

    viewModelOf(::SetupViewModel)

}