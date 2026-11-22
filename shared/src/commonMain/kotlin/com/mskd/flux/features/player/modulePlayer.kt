package com.mskd.flux.features.player

import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.player.data.PendingLaunchStoreImpl
import com.mskd.flux.features.player.domain.playback.PendingLaunchStore
import com.mskd.flux.features.player.domain.playback.PlaybackLaunch
import com.mskd.flux.features.player.domain.usecase.SaveTrackLanguageUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val modulePlayer = module {

    single<PendingLaunchStore> { PendingLaunchStoreImpl(dataStore = get(Qualifiers.PLAYBACK_DATASTORE)) }
    singleOf(::PlaybackLaunch)
    singleOf(::SaveTrackLanguageUseCase)

}