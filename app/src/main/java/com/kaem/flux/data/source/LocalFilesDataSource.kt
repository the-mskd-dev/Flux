package com.kaem.flux.data.source

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.kaem.flux.model.FileSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LocalFilesDataSource(
    private val context: Context
) : FilesDataSource {

    override suspend fun getFiles(): List<FileSource> {

        val files = mutableListOf<FileSource>()

        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION
        )

        // Show only videos that are at least 5 minutes in duration.
        val selection = "${MediaStore.Video.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString()
        )

        val query = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            null
        )

        withContext(Dispatchers.Main) {

            query?.use { cursor ->

                // Cache column indices.
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    // Get values of columns for a given video.
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    files += FileSource.Local(
                        name = name,
                        uri = contentUri
                    )

                }

            }

        }

        withContext(Dispatchers.Default) { delay(2000) }

        return files

    }
}