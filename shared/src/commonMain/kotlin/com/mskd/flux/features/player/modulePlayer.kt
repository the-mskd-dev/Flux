package com.mskd.flux.features.player

import com.mskd.flux.features.player.domain.usecase.ResolvePlaybackActionUseCase
import com.mskd.flux.features.player.domain.usecase.SaveTrackLanguageUseCase
import org.koin.dsl.module

val modulePlayer = module {

    single {
        ResolvePlaybackActionUseCase(
            settings = get()
        )
    }

    single {
        SaveTrackLanguageUseCase(
            settings = get()
        )
    }

}