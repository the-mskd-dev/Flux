package com.kaem.flux.data.source.file

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.kaem.flux.model.FileSource
import com.kaem.flux.model.UserFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class FilesDataSourceLocalImpl(
    private val context: Context
) : FilesDataSource {

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
        val selection = "${MediaStore.Video.Media.DURATION} >= ? AND ${MediaStore.Video.Media.MIME_TYPE} LIKE ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString(),
            "video/%"
        )

        val query = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            null
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

                        // Check if the file is a video
                        retriever.setDataSource(context, Uri.parse(contentPath))
                        val isVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) == "yes"

                        // Stores column values and the contentUri in a local object
                        // that represents the media file.
                        if (isVideo) {
                            files += UserFile(
                                name = name,
                                addedDateTime = date,
                                path = contentPath,
                                source = FileSource.LOCAL
                            )
                        }

                    } catch (e: Exception) {

                        Log.e("LocalFilesDataSource", "Fail to get file", e)

                    }

                }

            }

        }

        retriever.release()

        files.sortByDescending { it.addedDateTime }

        return files

    }

    override suspend fun checkIfFileExists(path: String): Boolean {

        val columns = arrayOf(MediaStore.Video.Media._ID)
        var result = true

        withContext(Dispatchers.Default) {

            val cursor = context.contentResolver.query(
                Uri.parse(path),
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