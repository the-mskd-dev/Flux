package com.mskd.flux.utils

import android.content.Intent
import androidx.core.net.toUri
import com.mskd.flux.model.artwork.Media

object ExternalPlayer {

    fun createIntent(media: Media) : Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(media.file.path.toUri(), "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            putExtra("position", media.currentTime)
            putExtra("extra_start_time", media.currentTime)
            putExtra("start_from", media.currentTime)
            putExtra("video_position", media.currentTime.toInt())

        }
    }
}