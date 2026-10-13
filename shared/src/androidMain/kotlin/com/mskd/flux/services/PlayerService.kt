package com.mskd.flux.services

import android.app.PendingIntent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.mskd.flux.di.QualifiersAndroid
import com.mskd.flux.features.player.data.manager.DecoderManagerHolder
import com.mskd.flux.report.Trace
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.DecoderManager
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatformTools

class PlayerService : MediaSessionService(), AndroidScopeComponent {

    private companion object {
        const val TAG = "PlayerService"
    }

    override val scope: Scope by lazy {
        KoinPlatformTools.defaultContext().get().createScope(
            scopeId = this.toString(),
            qualifier = QualifiersAndroid.PLAYER_SERVICE_SCOPE
        )
    }

    val player: Player by scope.inject()

    private val decoderManagerHolder: DecoderManagerHolder by lazy { scope.get() }

    private var mediaSession: MediaSession? = null

    private val mediaSessionCallback = object : MediaSession.Callback {

        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {

            val playerCommands = Player.Commands.Builder()
                .addAllCommands()
                .remove(Player.COMMAND_SEEK_FORWARD)
                .remove(Player.COMMAND_SEEK_BACK)
                .remove(Player.COMMAND_SEEK_TO_NEXT)
                .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder()
                .setAvailablePlayerCommands(playerCommands)
                .setAvailableSessionCommands(MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS)
                .build()
        }
    }

    override fun onCreate() {
        super.onCreate()

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setCallback(mediaSessionCallback)
            .build()

        // Resolving the player above built the ExoPlayer and attached this service's scoped
        // DecoderManager to it (see modulePlayerAndroid). Publish it so AndroidPlayerManager
        // can run runtime decoder recovery while this service is alive.
        decoderManagerHolder.decoderManager = scope.get<DecoderManager>()

    }

    override fun onGetSession(p0: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            // nextlib requires detaching the manager before releasing its player.
            detachDecoderManager()
            player.release()
            release()
            mediaSession = null
        }
        decoderManagerHolder.decoderManager = null
        scope.close()
        super.onDestroy()
    }

    private fun detachDecoderManager() {
        try {
            scope.getOrNull<DecoderManager>()?.detach()
        } catch (e: Exception) {
            Trace.error(TAG, "Failed to detach DecoderManager", e)
        }
    }
}