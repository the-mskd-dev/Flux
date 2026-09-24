package com.mskd.flux.report

import org.koin.dsl.module

val moduleReport = module {

    single<CrashLogger> { PlayCrashLogger() }

}