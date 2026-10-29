package com.mskd.flux.system

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Folder of the Gallery Image Downloads, relative to the primary storage. */
internal const val DOWNLOADED_IMAGES_PATH = "Pictures/Flux"

/**
 * Saves the images into `Pictures/Flux` through MediaStore, which needs no storage permission from minSdk 29.
 * A file name already taken is suffixed by the system, so nothing is overwritten.
 */
class AndroidImageSaver(private val context: Context) : ImageSaver {

    override suspend fun save(bytes: ByteArray, fileName: String, mimeType: String): Result<Unit> =
        withContext(Dispatchers.IO) {

            val resolver = context.contentResolver
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val pending = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, DOWNLOADED_IMAGES_PATH)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = try {
                resolver.insert(collection, pending)
            } catch (e: Exception) {
                Trace.error(TAG, "Fail to create $fileName", e)
                return@withContext Result.failure(e)
            } ?: return@withContext Result.failure(IllegalStateException("MediaStore refused to create $fileName"))

            var published = false
            try {
                val output = resolver.openOutputStream(uri)
                    ?: throw IllegalStateException("Cannot open $fileName for writing")
                output.use { it.write(bytes) }
                resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
                published = true
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Trace.error(TAG, "Fail to save $fileName", e)
                Result.failure(e)
            } finally {
                // Never leave a half written file behind
                if (!published) runCatching { resolver.delete(uri, null, null) }
            }

        }

    private companion object {
        const val TAG = "ImageSaver"
    }

}
