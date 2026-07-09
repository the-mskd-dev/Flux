package com.mskd.flux.features.files.data.usecase

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.usecase.GetFileDurationUseCase
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.msToMin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidGetFileDurationUseCase(private val context: Context) : GetFileDurationUseCase {

    override suspend fun invoke(file: UserFile): Int = withContext(Dispatchers.IO) {

        Trace.info(GetFileDurationUseCase.TAG, "Get duration for ${file.name}")

        val retriever = MediaMetadataRetriever()

        try {

            val duration = context.contentResolver.openAssetFileDescriptor(file.path.toUri(), "r")?.use { afd ->
                retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                val durationInMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                durationInMs.msToMin.toInt()
            } ?: 0

            duration

        } catch (e: Exception) {

            Trace.error(GetFileDurationUseCase.TAG, "Fail to get duration for ${file.path}", e)
            0

        } finally {

            retriever.release()

        }

    }

}