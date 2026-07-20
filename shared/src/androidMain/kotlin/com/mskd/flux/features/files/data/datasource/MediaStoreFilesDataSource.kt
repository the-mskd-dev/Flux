package com.mskd.flux.features.files.data.datasource

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.data.FileExtensions
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

class MediaStoreFilesDataSource(
    private val context: Context,
    private val userDataStore: UserDataStore
) : FilesDataSource {

    companion object {
        const val TAG = "MediaStoreFilesDataSource"
        private val STANDARD_FOLDERS = listOf(
            Environment.DIRECTORY_MOVIES,
            Environment.DIRECTORY_DOWNLOADS,
        )
    }

    override suspend fun getFiles(): List<UserFile> {

        updateMediaFolders()

        val files = mutableListOf<UserFile>()

        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        val (selection, selectionArgs) = buildRelativePathSelection()

        val query = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        val retriever = MediaMetadataRetriever()

        withContext(Dispatchers.IO) {

            query?.use { cursor ->

                // Cache column indices.
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

                while (cursor.moveToNext()) {

                    try {

                        // Get values of columns for a given video.
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn)
                        val date = cursor.getLong(dateColumn)

                        val contentPath = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id
                        ).toString()

                        // Stores column values and the contentUri in a local object
                        // that represents the media file.
                        files += UserFile(
                            name = name,
                            addedDateTime = date,
                            path = contentPath,
                            source = FileSource.LOCAL
                        )

                    } catch (e: Exception) {

                        Trace.error(TAG, "Fail to get file", e)

                    }

                }

            }

        }

        retriever.release()

        Trace.info(TAG, "Found ${files.size} files")
        files.forEach {
            Trace.info(TAG, it.name)
        }

        return files

    }

    override suspend fun filterExistingFiles(files: List<UserFile>): List<UserFile> =
        withContext(Dispatchers.IO) {

            val mediaStoreFiles = files.filter { it.source == FileSource.LOCAL }
            val paths = mediaStoreFiles.map { it.path }
            val ids = paths.mapNotNull { it.toUri().lastPathSegment }

            val idPlaceholders = ids.joinToString(",") { "?" }
            val (pathCondition, pathArgs) = buildRelativePathSelection()

            val selection = "${MediaStore.Video.Media._ID} IN ($idPlaceholders) AND ($pathCondition)"
            val selectionArgs = ids.toTypedArray() + pathArgs

            val existingIds = mutableSetOf<String>()

            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Video.Media._ID),
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                while (cursor.moveToNext()) {
                    existingIds.add(cursor.getString(idCol))
                }
            }

            val existingFiles = mediaStoreFiles.filter { file ->
                val id = file.path.toUri().lastPathSegment
                id in existingIds
            }

            val missingFiles = mediaStoreFiles - existingFiles.toSet()
            if (missingFiles.isNotEmpty()) {
                Trace.info(TAG, "$missingFiles file(s) not founded")
                missingFiles.forEach { Trace.info(TAG, it.name) }
            }

            existingFiles

        }

    override suspend fun getSubtitlesFor(file: UserFile): String? = withContext(Dispatchers.IO) {

        try {

            val mediaUri = file.path.toUri()
            val mediaId = mediaUri.lastPathSegment?.toLongOrNull() ?: return@withContext null

            // Get real path of file
            val videoPath = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Video.Media.DATA),
                "${MediaStore.Video.Media._ID} = ?",
                arrayOf(mediaId.toString()),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst())
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                else null
            } ?: return@withContext null

            val videoFile = File(videoPath)
            val baseName = videoFile.nameWithoutExtension
            val parentDir = videoFile.parentFile ?: return@withContext null

            // Get subtitles file, if exists, in the same directory, with the same name
            val subtitlesPath = FileExtensions.SUBTITLES
                .map { ext -> File(parentDir, "$baseName.$ext") }
                .firstOrNull { it.exists() }
                ?.toUri()?.toString()

            subtitlesPath

        } catch (e: Exception) {
            Trace.error(TAG, "Fail to get subtitles for ${file.name}", e)
            null
        }

    }

    /**
     * When you add files to your device, the Android system doesn't always update the file structure
     * immediately and may wait for a restart or file transfer.
     * This feature allows you to update the **Movies** and **Downloads** folders directly.
     */
    private suspend fun updateMediaFolders() {

        val lastSyncTime = userDataStore.getSyncTime()

        return suspendCancellableCoroutine { continuation ->

            val filesToScan =
                STANDARD_FOLDERS
                    .map { Environment.getExternalStoragePublicDirectory(it) }
                    .filter { it.exists() }
                    .flatMap { folder ->
                        folder.walkTopDown()
                            .filter { it.isFile }
                            .filter { lastSyncTime < it.lastModified() }
                            .filter { it.extension.lowercase() in FileExtensions.VIDEOS }
                            .toList()
                    }
                    .map { it.absolutePath }
                    .toTypedArray()

            if (filesToScan.isEmpty()) {
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }

            var filesScanned = 0

            MediaScannerConnection.scanFile(
                context,
                filesToScan,
                null
            ) { _, _ ->
                filesScanned++
                if (filesScanned >= filesToScan.size) {
                    continuation.resume(Unit)
                }
            }
        }
    }

    private fun buildRelativePathSelection(): Pair<String, Array<String>> {
        val condition = STANDARD_FOLDERS.joinToString(" OR ") {
            "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"
        }
        val args = STANDARD_FOLDERS.map { "$it/%" }.toTypedArray()
        return condition to args
    }

}