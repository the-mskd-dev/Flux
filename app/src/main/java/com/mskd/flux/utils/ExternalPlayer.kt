package com.mskd.flux.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.services.ExternalPlayerService

@Composable
fun rememberExternalPlayerLauncher(context: Context, onProgressResult: (Long) -> Unit) : ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        ExternalPlayerService.stop(context)
        ExternalPlayer.parsePosition(result.data)?.let {
            onProgressResult(it)
        }
    }
}

object ExternalPlayer {

    fun launchPlayer(
        context: Context,
        media: Media,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        onError: () -> Unit
    ) {
        try {
            val intent = createIntent(media = media)
            ExternalPlayerService.start(context)
            launcher.launch(intent)
        } catch (e: Exception) {
            when (e) {
                is ActivityNotFoundException -> Log.e("ExternalPlayer", "No player found", e)
                is SecurityException -> Log.e("ExternalPlayer", "Permission denied", e)
                else -> Log.e("ExternalPlayer", "Fail to launch external player", e)
            }
            ExternalPlayerService.stop(context)
            onError()
        }
    }

    private fun createIntent(media: Media) : Intent {
        return Intent(Intent.ACTION_VIEW).apply {

            setDataAndType(media.file.uri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra("return_result", true)

            val progress = media.currentTime.coerceAtLeast(10L)
            startingProgressFlags.forEach { putExtra(it, progress) }
            titleFlags.forEach { putExtra(it, media.title) }

        }
    }

    fun parsePosition(data: Intent?): Long? {
        if (data == null) return null

        for (key in resultProgressFlags) {
            val longPos = data.getLongExtra(key, -1L)
            if (longPos != -1L) return longPos

            val intPos = data.getIntExtra(key, -1)
            if (intPos != -1) return intPos.toLong()
        }

        return null
    }

    private val titleFlags = listOf(
        "title",
        "android.intent.extra.TITLE",
        "filename",
        "media_title"
    )

    private val startingProgressFlags = listOf(
        "position",
        "extra_position",
        "extra_start_time",
        "start_from",
        "video_position",
        "resume_from",
        "from_start",
        "start_position",
        "playback_start"
    )

    private val resultProgressFlags = listOf(
        "position",
        "extra_position",
        "playback_position",
        "end_by",
    )
}