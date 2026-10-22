package com.mskd.flux.services

import android.app.PendingIntent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.mskd.flux.features.player.domain.manager.SeekPrecision
import com.mskd.flux.di.QualifiersAndroid
import com.mskd.flux.features.player.data.manager.DecoderManagerHolder
import com.mskd.flux.report.Trace
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.DecoderManager
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatformTools

class PlayerService : MediaSessionService(), AndroidScopeComponent {

    companion object {
        private const val TAG = "PlayerService"
        private const val ACTION_SET_SEEK_PRECISION = "com.mskd.flux.SET_SEEK_PRECISION"
        private const val EXTRA_FAST = "fast"

        fun setSeekPrecisionCommand(precision: SeekPrecision): SessionCommand =
            SessionCommand(ACTION_SET_SEEK_PRECISION, bundleOf(EXTRA_FAST to (precision == SeekPrecision.FAST)))
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
                .setAvailableSessionCommands(
                    MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
                        .add(SessionCommand(ACTION_SET_SEEK_PRECISION, Bundle.EMPTY))
                        .build()
                )
                .build()
        }

        @OptIn(UnstableApi::class)
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == ACTION_SET_SEEK_PRECISION) {
                val fast = customCommand.customExtras.getBoolean(EXTRA_FAST)
                (session.player as? ExoPlayer)?.setSeekParameters(
                    if (fast) SeekParameters.CLOSEST_SYNC else SeekParameters.EXACT
                )
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
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