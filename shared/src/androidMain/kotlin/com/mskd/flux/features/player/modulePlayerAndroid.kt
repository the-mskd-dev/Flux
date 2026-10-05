@file:OptIn(androidx.media3.common.util.UnstableApi::class)
package com.mskd.flux.features.player

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mp4.Mp4Extractor
import com.mskd.flux.di.QualifiersAndroid
import com.mskd.flux.features.player.data.PipIsEnabledUseCase
import com.mskd.flux.features.player.data.manager.AndroidPlayerManager
import com.mskd.flux.features.player.data.usecase.AndroidPipIsEnabledUseCase
import com.mskd.flux.features.player.domain.manager.PlayerManager
import com.mskd.flux.features.player.presentation.PlayerViewModel
import com.mskd.flux.utils.Trace
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.DecoderManager
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private const val TAG = "modulePlayerAndroid"

val modulePlayerAndroid = module {

    // One manager per player: selects the decoder (hardware / software / FFmpeg)
    // and enables runtime decoder fallback through NextRenderersFactory.
    single { DecoderManager() }

    scope(QualifiersAndroid.PLAYER_SERVICE_SCOPE) {

        scoped<Player> {
            val context = androidContext()
            val decoderManager = get<DecoderManager>()

            val extractorsFactory = DefaultExtractorsFactory()
                .setMp4ExtractorFlags(Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS)

            val mediaSourceFactory = DefaultMediaSourceFactory(context, extractorsFactory)

            ExoPlayer.Builder(context)
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .setHandleAudioBecomingNoisy(true)
                .setMediaSourceFactory(mediaSourceFactory)
                .setRenderersFactory(
                    // setDecoderManager enables FFmpeg extension renderers at normal
                    // priority (ON) and MediaCodec initialization fallback.
                    NextRenderersFactory(context).setDecoderManager(decoderManager)
                )
                .build()
                .apply {
                    //playWhenReady = true
                    setSeekParameters(SeekParameters.CLOSEST_SYNC)
                }
                .also { player ->
                    try {
                        // detach() is idempotent: guards against a service restart where
                        // the previous player was released without detaching.
                        decoderManager.detach()
                        // Must be attached before any prepare. The scope is injected from
                        // PlayerService.onCreate on the main (application) thread.
                        decoderManager.attach(player)
                    } catch (e: Exception) {
                        // Keep playback working without runtime decoder recovery
                        // rather than failing the player build.
                        Trace.error(TAG, "Failed to attach DecoderManager", e)
                    }
                }
        }
    }

    factory<PlayerManager<Player>> {
        AndroidPlayerManager(
            context = androidContext(),
            saveTrackLanguageUseCase = get(),
            decoderManager = get()
        )
    }

    viewModel { params ->
        PlayerViewModel<Player>(
            params = params.get(),
            observeArtworkUseCase = get(),
            settingsDataStore = get(),
            playerManager = get(),
            pipIsEnabledUseCase = get(),
            saveProgressUseCase = get(),
            getSubtitlesUseCase = get(),
            saveTrackLanguageUseCase = get(),
        )
    }

    single<PipIsEnabledUseCase> {
        AndroidPipIsEnabledUseCase(
            context = androidContext(),
            settingsDataStore = get()
        )
    }

}