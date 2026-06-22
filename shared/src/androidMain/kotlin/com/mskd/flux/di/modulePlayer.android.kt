@file:OptIn(UnstableApi::class)
package com.mskd.flux.di

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.mskd.flux.screen.player.AndroidPlayerManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val modulePlayer = module {

    singleOf(::AndroidPlayerManager)

    scope(named("PlayerServiceScope")) {

        scoped<Player> {
            val context = androidContext()

            ExoPlayer.Builder(context)
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .setHandleAudioBecomingNoisy(true)
                .setRenderersFactory(
                    DefaultRenderersFactory(context)
                        .setExtensionRendererMode(
                            DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                        )
                )
                .build()
                .apply {
                    //playWhenReady = true
                    setSeekParameters(SeekParameters.CLOSEST_SYNC)
                }
        }
    }

    viewModel { params ->
        PlayerViewModel(
            mediaId = params.get(),
            artworkUC = get(),
            settingsRepository = get(),
            filesRepository = get(),
            playerManager = get(),
            progressUC = get(),
            pipIsEnabledUC = get()
        )
    }
}