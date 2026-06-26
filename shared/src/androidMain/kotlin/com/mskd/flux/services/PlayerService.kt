package com.mskd.flux.services

import android.app.PendingIntent
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.mskd.flux.di.QualifiersAndroid
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatformTools

class PlayerService : MediaSessionService(), AndroidScopeComponent {

    override val scope: Scope by lazy {
        KoinPlatformTools.defaultContext().get().createScope(
            scopeId = this.toString(),
            qualifier = QualifiersAndroid.PLAYER_SERVICE_SCOPE
        )
    }

    val player: Player by scope.inject()

    private var mediaSession: MediaSession? = null

    private val mediaSessionCallback = object : MediaSession.Callback {

        @androidx.annotation.OptIn(UnstableApi::class)
        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {

            val connectionResult = super.onConnect(session, controller)
            val availableCommands = connectionResult.availableSessionCommands.buildUpon()

            val playerCommands = connectionResult.availablePlayerCommands.buildUpon()
                .remove(Player.COMMAND_SEEK_FORWARD)
                .remove(Player.COMMAND_SEEK_BACK)
                .remove(Player.COMMAND_SEEK_TO_NEXT)
                .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                .build()

            return MediaSession.ConnectionResult.accept(
                availableCommands.build(),
                playerCommands
            )
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

    }

    override fun onGetSession(p0: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        scope.close()
        super.onDestroy()
    }
}