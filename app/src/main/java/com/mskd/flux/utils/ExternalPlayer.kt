package com.mskd.flux.utils

import android.content.Intent
import androidx.core.net.toUri
import com.mskd.flux.model.artwork.Media

object ExternalPlayer {

    fun createIntent(media: Media) : Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(media.file.path.toUri(), "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            progressFlags.forEach { flag ->
                putExtra(flag, media.currentTime)
            }
        }
    }

    private val progressFlags = listOf(
        "position",
        "extra_start_time",
        "start_from",
        "video_position"
    )
}