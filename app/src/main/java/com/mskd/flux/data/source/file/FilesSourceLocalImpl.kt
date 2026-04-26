package com.mskd.flux.data.source.file

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.mskd.flux.model.FileSource
import com.mskd.flux.model.UserFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class FilesSourceLocalImpl(
    private val context: Context
) : FilesSource {

    override suspend fun getFiles(): List<UserFile> {

        val files = mutableListOf<UserFile>()

        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED
        )

        // Show only videos that are at least 5 minutes in duration.
        val selection = "${MediaStore.Video.Media.DURATION} >="
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString(),
        )

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

                        Log.e("LocalFilesDataSource", "Fail to get file", e)

                    }

                }

            }

        }

        retriever.release()

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
}