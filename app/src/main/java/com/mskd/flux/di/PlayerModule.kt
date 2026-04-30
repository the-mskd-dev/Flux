package com.mskd.flux.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.mskd.flux.screens.player.controllers.PlayerManager
import com.mskd.flux.services.ExternalPlayerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ServiceComponent::class)
object PlayerModule {

    @Provides
    @ServiceScoped
    @OptIn(UnstableApi::class)
    fun providePlayer(@ApplicationContext context: Context) : Player {
        return ExoPlayer.Builder(context)
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

@Module
@InstallIn(SingletonComponent::class)
object PlayerManagerModule {

    @Provides
    @Singleton
    fun providePlayerManager(@ApplicationContext context: Context) : PlayerManager {
        return PlayerManager(context)
    }

    @Provides
    @Singleton
    fun provideExternalPlayerManager(@ApplicationContext context: Context) : ExternalPlayerManager {
        return ExternalPlayerManager(context)
    }
}