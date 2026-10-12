package com.mskd.flux.features.player

import com.mskd.flux.features.player.domain.usecase.GetNextEpisodeUseCase
import com.mskd.flux.features.player.domain.usecase.ResolvePlaybackActionUseCase
import com.mskd.flux.features.player.domain.usecase.SaveTrackLanguageUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val modulePlayer = module {

    singleOf(::ResolvePlaybackActionUseCase)
    singleOf(::SaveTrackLanguageUseCase)
    singleOf(::GetNextEpisodeUseCase)

}