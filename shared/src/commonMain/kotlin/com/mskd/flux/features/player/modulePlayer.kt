package com.mskd.flux.features.player

import com.mskd.flux.features.player.domain.usecase.RecordPlaybackResultUseCase
import com.mskd.flux.features.player.domain.usecase.ResolvePlaybackActionUseCase
import org.koin.dsl.module

val modulePlayer = module {

    single {
        ResolvePlaybackActionUseCase(
            settings = get()
        )
    }

    single {
        RecordPlaybackResultUseCase(
            saveProgress = get(),
            saveToHistory = get()
        )
    }

}