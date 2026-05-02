package com.mskd.flux.utils

import android.content.Context
import android.content.Intent
import com.mskd.flux.model.artwork.Media

object ExternalPlayer {

    fun createIntent(media: Media, context: Context) : Intent {

        val uri = media.file.resolvedUri(context)

        return Intent(Intent.ACTION_VIEW).apply {

            setDataAndType(uri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra("return_result", true)
            progressFlags.forEach { putExtra(it, media.currentTime) }

        }
    }

    fun parsePosition(data: Intent?): Long? {
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