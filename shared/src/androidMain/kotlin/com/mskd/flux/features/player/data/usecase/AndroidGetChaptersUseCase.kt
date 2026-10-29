package com.mskd.flux.features.player.data.usecase

import android.content.Context
import androidx.core.net.toUri
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.domain.model.VideoChapterType
import com.mskd.flux.features.player.domain.usecase.GetChaptersUseCase
import com.mskd.flux.report.Trace
import io.github.anilbeesetti.nextlib.mediainfo.MediaInfoBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidGetChaptersUseCase(
    private val context: Context,
    private val createMediaInfoBuilder: () -> MediaInfoBuilder = { MediaInfoBuilder() }
) : GetChaptersUseCase {

    override suspend fun invoke(media: Media): List<VideoChapter> = withContext(Dispatchers.IO) {

        val info = try {
            createMediaInfoBuilder().from(context, media.file.path.toUri()).build()
        } catch (e: Exception) {
            Trace.error(TAG, "Failed to read chapters", e)
            null
        }

        try {

            info?.chapters?.map {
                VideoChapter(
                    index = it.index,
                    start = it.start,
                    end = it.end,
                    type = VideoChapterType.fromString(it.title),
                    title = it.title?.takeUnless { title -> title.isBlank() }
                )
            }.orEmpty().also { Trace.info(TAG, "Found ${it.size} chapter(s)") }

        } finally {

            info?.release()
        }

    }

    companion object {
        const val TAG = "AndroidGetChaptersUseCase"
    }

}