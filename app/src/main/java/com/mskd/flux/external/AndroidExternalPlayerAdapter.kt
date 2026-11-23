package com.mskd.flux.external

import android.app.Activity
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

    /**
     * Result keys and the factor converting each to Progress milliseconds (`Media.currentTime`).
     * MX Player and mpv report `position` in ms (Int), VLC `extra_position` in ms (Long); the other keys
     * follow the same convention. Change a factor here if a player is found to report another unit.
     */
    private val resultPositionMsFactor = linkedMapOf(
        "position" to 1L, "extra_position" to 1L, "playback_position" to 1L, "end_by" to 1L,
    )

    /** Result extras an External player sets, with a cancelled result, to report that it failed. */
    private val errorKeys = listOf("error", "error_message", "extra_error")

    /** A cancelled result carrying one of the error extras is a failure; a plain cancel is the user closing the player. */
    fun isFailure(resultCode: Int, read: (String) -> Any?): Boolean =
        resultCode == Activity.RESULT_CANCELED && errorKeys.any { read(it) != null }

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

    /** Position reported by the External player, converted to Progress milliseconds; null when absent. */
    fun parsePosition(read: (String) -> Any?): Long? {
        for ((key, factor) in resultPositionMsFactor) {
            val reported = when (val value = read(key)) {
                is Long -> value
                is Int -> value.toLong()
                else -> continue
            }
            if (reported >= 0L) return reported * factor
        }
        return null
    }
}

class AndroidExternalPlayerAdapter(context: Context) : ExternalPlayerAdapter {

    // Replaced in tests: the foreground service and Intent building need a device.
    internal var startService: () -> Unit = { ExternalPlayerService.start(context) }
    internal var stopService: () -> Unit = { ExternalPlayerService.stop(context) }
    internal var createIntent: (Media) -> Intent = ::viewIntent

    private var launcher: ActivityResultLauncher<Intent>? = null

    fun attach(launcher: ActivityResultLauncher<Intent>) {
        this.launcher = launcher
    }

    /** Only detaches [launcher] itself: a newer Activity may already have attached its own. */
    fun detach(launcher: ActivityResultLauncher<Intent>) {
        if (this.launcher === launcher) this.launcher = null
    }

    override fun launch(media: Media): ExternalLaunchResult {
        val launcher = launcher ?: return ExternalLaunchResult.Failed(IllegalStateException("No activity result launcher attached"))
        return try {
            startService()
            launcher.launch(createIntent(media))
            ExternalLaunchResult.Launched
        } catch (e: ActivityNotFoundException) {
            stopService()
            ExternalLaunchResult.NoPlayerInstalled
        } catch (e: SecurityException) {
            stopService()
            ExternalLaunchResult.PermissionDenied
        } catch (e: Exception) {
            stopService()
            ExternalLaunchResult.Failed(e)
        }
    }

    /** Stops the foreground service and describes what the External player reported. */
    fun onResult(resultCode: Int, data: Intent?): ExternalPlayerResult {
        stopService()
        val extras = data?.extras ?: return ExternalPlayerResult.Finished(null)
        @Suppress("DEPRECATION")
        val read = { key: String -> extras.get(key) }
        if (ExternalPlayerRequest.isFailure(resultCode, read)) {
            return ExternalPlayerResult.Failed(IllegalStateException("External player reported an error: ${read("error")}"))
        }
        return ExternalPlayerResult.Finished(ExternalPlayerRequest.parsePosition(read))
    }

}

/** What an External player reported through the activity result. */
sealed interface ExternalPlayerResult {
    /** It closed; [position] is the Progress it reported, null when none. */
    data class Finished(val position: Long?) : ExternalPlayerResult
    /** It failed after it started. */
    data class Failed(val cause: Throwable) : ExternalPlayerResult
}

private fun viewIntent(media: Media): Intent = Intent(Intent.ACTION_VIEW).apply {
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
