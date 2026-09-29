package com.mskd.flux.features.message

import com.mskd.flux.features.message.presentation.MessageViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleMessage = module {

    viewModelOf(::MessageViewModel)

}