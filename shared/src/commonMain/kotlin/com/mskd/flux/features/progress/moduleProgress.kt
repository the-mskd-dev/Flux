package com.mskd.flux.features.progress

import com.mskd.flux.features.progress.domain.ProgressTracker
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val moduleProgress = module {

    singleOf(::ProgressTracker)

}