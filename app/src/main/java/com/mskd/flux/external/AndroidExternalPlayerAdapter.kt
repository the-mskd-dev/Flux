package com.mskd.flux.external

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.player.domain.playback.ExternalLaunchResult
import com.mskd.flux.features.player.domain.playback.ExternalPlayerAdapter
import com.mskd.flux.services.ExternalPlayerService

/** Pure description of the view request sent to an External player (testable without Android). */
object ExternalPlayerRequest {

    private val titleKeys = listOf("title", "android.intent.extra.TITLE", "filename", "media_title")

    private val startPositionKeys = listOf(
        "position", "extra_position", "extra_start_time", "start_from",
        "video_position", "resume_from", "start_position", "playback_start",
    )

    private val resultPositionKeys = listOf("position", "extra_position", "playback_position", "end_by")

    /** Extras of the request: start at the saved Progress, or from the start when there is none. */
    fun extras(title: String, currentTime: Long): Map<String, Any> = buildMap {
        put("return_result", true)
        titleKeys.forEach { put(it, title) }
        if (currentTime > 0L) {
            startPositionKeys.forEach { put(it, currentTime) }
        } else {
            put("from_start", true)
            startPositionKeys.forEach { put(it, 1L) }
        }
    }

    /** Position reported by the External player: Long or Int under one of several keys, null when absent. */
    fun parsePosition(read: (String) -> Any?): Long? {
        for (key in resultPositionKeys) {
            val position = when (val value = read(key)) {
                is Long -> value
                is Int -> value.toLong()
                else -> continue
            }
            if (position != -1L) return position
        }
        return null
    }
}

class AndroidExternalPlayerAdapter(private val context: Context) : ExternalPlayerAdapter {

    private var launcher: ActivityResultLauncher<Intent>? = null

    fun attach(launcher: ActivityResultLauncher<Intent>) {
        this.launcher = launcher
    }

    fun detach() {
        launcher = null
    }

    override fun launch(media: Media): ExternalLaunchResult {
        val launcher = launcher ?: return ExternalLaunchResult.Failed(IllegalStateException("No activity result launcher attached"))
        return try {
            ExternalPlayerService.start(context)
            launcher.launch(createIntent(media))
            ExternalLaunchResult.Launched
        } catch (e: ActivityNotFoundException) {
            ExternalPlayerService.stop(context)
            ExternalLaunchResult.NoPlayerInstalled
        } catch (e: SecurityException) {
            ExternalPlayerService.stop(context)
            ExternalLaunchResult.PermissionDenied
        } catch (e: Exception) {
            ExternalPlayerService.stop(context)
            ExternalLaunchResult.Failed(e)
        }
    }

    /** Stops the foreground service and returns the reported position, if any. */
    fun onResult(data: Intent?): Long? {
        ExternalPlayerService.stop(context)
        val extras = data?.extras ?: return null
        @Suppress("DEPRECATION")
        return ExternalPlayerRequest.parsePosition { key -> extras.get(key) }
    }

    private fun createIntent(media: Media): Intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(media.file.path.toUri(), "video/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        ExternalPlayerRequest.extras(title = media.title, currentTime = media.currentTime).forEach { (key, value) ->
            when (value) {
                is Boolean -> putExtra(key, value)
                is Long -> putExtra(key, value)
                is String -> putExtra(key, value)
            }
        }
    }
}
