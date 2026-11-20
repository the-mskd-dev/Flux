package com.mskd.flux.features.player

import com.mskd.flux.features.player.domain.playback.PlaybackLaunch
import com.mskd.flux.features.player.domain.usecase.GetNextEpisodeUseCase
import com.mskd.flux.features.player.domain.usecase.SaveTrackLanguageUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val modulePlayer = module {

    singleOf(::PlaybackLaunch)
    singleOf(::SaveTrackLanguageUseCase)
    singleOf(::GetNextEpisodeUseCase)

}