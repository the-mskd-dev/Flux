package com.mskd.flux.services

import android.app.PendingIntent
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatformTools

class PlayerService : MediaSessionService(), AndroidScopeComponent {

    override val scope: Scope by lazy {
        KoinPlatformTools.defaultContext().get().createScope(
            scopeId = this.toString(),
            qualifier = named("PlayerServiceScope")
        )
    }

    val player: Player by scope.inject()

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
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