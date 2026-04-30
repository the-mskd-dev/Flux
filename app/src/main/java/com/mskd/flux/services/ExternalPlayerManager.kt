package com.mskd.flux.services

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExternalPlayerManager(private val context: Context) {

    private var mediaSessionManager: MediaSessionManager? = null
    private var sessionListener: MediaSessionManager.OnActiveSessionsChangedListener? = null

    private val _position = MutableStateFlow<Long?>(null)
    val position = _position.asStateFlow()

    // Appelé avant de lancer le player externe
    fun startWatching(startPosition: Long) {
        _position.value = startPosition

        mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE)
                as? MediaSessionManager

        sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { sessions ->
            updatePositionFromSessions(sessions)
        }

        try {
            mediaSessionManager?.addOnActiveSessionsChangedListener(
                sessionListener!!,
                ComponentName(context, ExternalPlayerService::class.java)
            )
        } catch (e: SecurityException) {
            // Permission non accordée
            Log.e("ExternalPlayerManager", "Permission not granted", e)
        }
    }

    // Appelé quand l'utilisateur revient dans l'app
    fun stopWatching(): Long? {
        val lastPosition = getCurrentExternalPosition()

        sessionListener?.let {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(it)
        }
        sessionListener = null
        mediaSessionManager = null

        return lastPosition
    }

    private fun getCurrentExternalPosition(): Long? {
        return try {
            mediaSessionManager
                ?.getActiveSessions(ComponentName(context, ExternalPlayerService::class.java))
                ?.firstOrNull { it.packageName != context.packageName }
                ?.playbackState
                ?.position
        } catch (e: SecurityException) {
            null
        }
    }

    private fun updatePositionFromSessions(sessions: List<MediaController>?) {
        sessions
            ?.firstOrNull { it.packageName != context.packageName }
            ?.playbackState
            ?.position
            ?.let { _position.value = it }
    }

    fun isPermissionGranted(): Boolean {
        return try {
            mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE)
                    as? MediaSessionManager
            mediaSessionManager?.getActiveSessions(
                ComponentName(context, ExternalPlayerService::class.java)
            ) != null
        } catch (e: SecurityException) {
            false
        }
    }
}