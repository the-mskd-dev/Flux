package com.mskd.flux.features.unknown

import com.mskd.flux.features.unknown.presentation.UnknownViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleUnknown = module {

    viewModelOf(::UnknownViewModel)

}