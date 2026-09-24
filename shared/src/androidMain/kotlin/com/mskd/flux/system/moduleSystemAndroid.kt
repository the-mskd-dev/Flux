package com.mskd.flux.system

import org.koin.dsl.module

val moduleSystemAndroid = module {

    single<EmailLauncher> { AndroidEmailLauncher(get()) }
}