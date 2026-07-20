@file:OptIn(androidx.media3.common.util.UnstableApi::class)
package com.mskd.flux.features.player

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mp4.Mp4Extractor
import com.mskd.flux.di.QualifiersAndroid
import com.mskd.flux.features.player.data.PipIsEnabledUseCase
import com.mskd.flux.features.player.data.usecase.AndroidPipIsEnabledUseCase
import com.mskd.flux.features.player.presentation.PlayerViewModel
import com.mskd.flux.platform.AndroidPlayerManager
import com.mskd.flux.platform.PlayerManager
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
            observeArtworkUseCase = get(),
            settingsDataStore = get(),
            playerManager = get(),
            pipIsEnabledUseCase = get(),
            saveProgressUseCase = get(),
            getSubtitlesUseCase = get()
        )
    }

    single<PipIsEnabledUseCase> {
        AndroidPipIsEnabledUseCase(
            context = androidContext(),
            settingsDataStore = get()
        )
    }

}