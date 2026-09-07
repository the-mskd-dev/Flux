package com.mskd.flux.features.progress

import com.mskd.flux.features.progress.domain.usecase.ChangeMediaStatusUseCase
import com.mskd.flux.features.progress.domain.usecase.MarkPreviousAsWatchedUseCase
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val moduleProgress = module {

    singleOf(::ChangeMediaStatusUseCase)
    singleOf(::MarkPreviousAsWatchedUseCase)
    singleOf(::ResetProgressUseCase)
    singleOf(::SaveProgressUseCase)

}