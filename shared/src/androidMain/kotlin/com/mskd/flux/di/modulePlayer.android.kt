@file:OptIn(UnstableApi::class)
package com.mskd.flux.di

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mp4.Mp4Extractor
import com.mskd.flux.platform.AndroidPlayerManager
import com.mskd.flux.platform.PlayerManager
import com.mskd.flux.screen.player.PlayerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val modulePlayerAndroid = module {

    scope(QualifiersAndroid.PLAYER_SERVICE_SCOPE) {

        scoped<Player> {
            val context = androidContext()

            val extractorsFactory = DefaultExtractorsFactory()
                .setMp4ExtractorFlags(Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS)

            val mediaSourceFactory = DefaultMediaSourceFactory(context, extractorsFactory)

            ExoPlayer.Builder(context)
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .setHandleAudioBecomingNoisy(true)
                .setMediaSourceFactory(mediaSourceFactory)
                .setRenderersFactory(
                    DefaultRenderersFactory(context)
                        .setExtensionRendererMode(
                            DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                        )
                        .setEnableDecoderFallback(true)
                )
                .build()
                .apply {
                    //playWhenReady = true
                    setSeekParameters(SeekParameters.CLOSEST_SYNC)
                }
        }
    }

    factory<PlayerManager<Player>> {
        AndroidPlayerManager(
            context = androidContext()
        )
    }

    viewModel { params ->
        PlayerViewModel<Player>(
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