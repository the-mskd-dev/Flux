package com.mskd.flux.platform

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import com.mskd.flux.model.UserFile
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.msToMin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidMetadataProvider(private val context: Context) : MetadataProvider {

    private companion object {
        const val TAG = "AndroidMetadataProvider"
    }


    override suspend fun getDuration(file: UserFile): Int = withContext(Dispatchers.IO) {

        Trace.info(TAG, "Get duration for ${file.name}")

        val retriever = MediaMetadataRetriever()

        try {

            val duration = context.contentResolver.openAssetFileDescriptor(file.path.toUri(), "r")?.use { afd ->
                retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                val durationInMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                durationInMs.msToMin.toInt()
            } ?: 0

            duration

        } catch (e: Exception) {

            Trace.error(TAG, "Fail to get duration for ${file.path}", e)
            0

        } finally {

            retriever.release()

        }

    }

}