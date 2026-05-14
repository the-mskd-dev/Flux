package com.mskd.flux.data.repository.files

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.model.FileSource
import com.mskd.flux.model.UserFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class FilesRepositoryImpl(
    private val context: Context,
    private val userRepository: UserRepository
) : FilesRepository {

    companion object {
        const val TAG = "FilesRepositoryImpl"
        private val STANDARD_FOLDERS = listOf(
            Environment.DIRECTORY_MOVIES,
            Environment.DIRECTORY_DOWNLOADS,
        )
        val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "avi", "mov", "webm", "ts", "m4v")
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

        // Show only videos that are at least 5 minutes in duration, or those whose system has not found a duration
        val minDuration = TimeUnit.MINUTES.toMillis(5).toString()
        val selection = "${MediaStore.Video.Media.DURATION} >= ? OR " +
                "${MediaStore.Video.Media.DURATION} = 0 OR " +
                "${MediaStore.Video.Media.DURATION} IS NULL"
        val selectionArgs = arrayOf(minDuration)

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

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

                        Log.e(TAG, "Fail to get file", e)

                    }

                }

            }

        }

        retriever.release()

        Log.i(TAG, "Found ${files.size} files")
        files.forEach {
            Log.i(TAG, it.name)
        }

        return files

    }

    override suspend fun filterExistingFiles(files: List<UserFile>): List<UserFile> = withContext(Dispatchers.IO) {

        val paths = files.map { it.path }
        val ids = paths.mapNotNull { it.toUri().lastPathSegment }

        val placeholders = ids.joinToString(",") { "?" }

        val existingIds = mutableSetOf<String>()

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Video.Media._ID),
            "${MediaStore.Video.Media._ID} IN ($placeholders)",
            ids.toTypedArray(),
            null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            while (cursor.moveToNext()) {
                existingIds.add(cursor.getString(idCol))
            }
        }

        val existingFiles = files.filter { file ->
            val id = file.path.toUri().lastPathSegment
            id in existingIds
        }

        val missingFiles = files - existingFiles.toSet()
        if (missingFiles.isNotEmpty()) {
            Log.i(TAG, "$missingFiles file(s) not founded")
            missingFiles.forEach { Log.i(TAG, it.name) }
        }

        existingFiles

    }

    override suspend fun getSubtitlesFor(file: UserFile): Uri? = withContext(Dispatchers.IO) {

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
            val subtitleExtensions = listOf("srt", "vtt", "ass", "ssa")
            val subtitleFile = subtitleExtensions
                .map { ext -> File(parentDir, "$baseName.$ext") }
                .firstOrNull { it.exists() }

            subtitleFile?.toUri()

        } catch (e: Exception) {
            Log.e(TAG, "Fail to get subtitles for ${file.name}", e)
            null
        }

    }

    /**
     * When you add files to your device, the Android system doesn't always update the file structure
     * immediately and may wait for a restart or file transfer.
     * This feature allows you to update the **Movies** and **Downloads** folders directly.
     */
    private suspend fun updateMediaFolders() {

        val lastSyncTime = userRepository.getSyncTime()

        return suspendCancellableCoroutine { continuation ->

            val filesToScan =
                STANDARD_FOLDERS
                    .map { Environment.getExternalStoragePublicDirectory(it) }
                    .filter { it.exists() }
                    .flatMap { folder ->
                        folder.walkTopDown()
                            .filter { it.isFile }
                            .filter { lastSyncTime < it.lastModified() }
                            .filter { it.extension.lowercase() in VIDEO_EXTENSIONS }
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
}