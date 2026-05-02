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
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.services.ExternalPlayerService

object ExternalPlayer {

    @Composable
    fun launcher(context: Context, onProgressResult: (Long) -> Unit) : ManagedActivityResultLauncher<Intent, ActivityResult> {
        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            ExternalPlayerService.stop(context)
            parsePosition(result.data)?.let {
                onProgressResult(it)
            }
        }
    }

    fun launchPlayer(
        context: Context,
        media: Media,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        onError: () -> Unit
    ) {
        try {
            val intent = createIntent(media = media, context = context)
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

    private fun createIntent(media: Media, context: Context) : Intent {

        val uri = media.file.resolvedUri(context)

        return Intent(Intent.ACTION_VIEW).apply {

            setDataAndType(uri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra("return_result", true)
            progressFlags.forEach { putExtra(it, media.currentTime) }

        }
    }

    private fun parsePosition(data: Intent?): Long? {
        if (data == null) return null

        val intPosition = data.getIntExtra("position", -1)
        if (intPosition != -1) return intPosition.toLong()

        val longPosition = data.getLongExtra("extra_position", -1L)
        if (longPosition != -1L) return longPosition

        return null
    }

    private val progressFlags = listOf(
        "position",
        "extra_start_time",
        "start_from",
        "video_position"
    )
}