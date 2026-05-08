package com.mskd.flux.data.repository.files

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.data.source.file.FilesSource
import com.mskd.flux.data.source.file.FilesSourceLocalImpl
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
) : FilesSource {

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

                        Log.e(FilesSourceLocalImpl.Companion.TAG, "Fail to get file", e)

                    }

                }

            }

        }

        retriever.release()

        Log.i(FilesSourceLocalImpl.Companion.TAG, "Found ${files.size} files")
        files.forEach {
            Log.i(FilesSourceLocalImpl.Companion.TAG, it.name)
        }

        return files

    }

    override suspend fun checkIfFileExists(path: String): Boolean {

        val columns = arrayOf(MediaStore.Video.Media._ID)
        var result = true

        withContext(Dispatchers.Default) {

            val cursor = context.contentResolver.query(
                path.toUri(),
                columns, // Empty projections are bad for performance
                null,
                null,
                null)

            result = cursor?.moveToFirst() ?: false

            cursor?.close()

        }

        return result

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