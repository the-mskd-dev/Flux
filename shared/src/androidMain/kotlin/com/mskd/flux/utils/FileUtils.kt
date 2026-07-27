package com.mskd.flux.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.net.toUri
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FileUtils {

    const val TAG = "FileUtils"

    suspend fun openFileExplorer(context: Context, file: UserFile) {
        val targetUri = withContext(Dispatchers.IO) {
            resolveTargetUri(context, file)
        }

        try {
            launchExplorer(context, targetUri)
        } catch (e: Exception) {
            Trace.error(tag = TAG, message = "File to open file explorer", throwable = e)
        }
    }

    private fun resolveTargetUri(context: Context, file: UserFile): Uri? {
        return when (file.source) {
            FileSource.SAF -> file.path.toUri()
            FileSource.LOCAL -> {
                val (root, folder) = file.realPath.takeIf { it.isNotBlank() }
                    ?.let { "primary" to it.substringBeforeLast('/', missingDelimiterValue = "") }
                    ?: fetchDocumentRootAndFolder(context, file.path)
                    ?: return null

                if (folder.isEmpty()) return null
                DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    "$root:$folder",
                )
            }
        }
    }

    private fun fetchDocumentRootAndFolder(context: Context, path: String): Pair<String, String>? {
        val mediaId = path.toUri().lastPathSegment?.toLongOrNull() ?: return null

        val (relativePath, volumeName) = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Video.Media.RELATIVE_PATH, MediaStore.Video.Media.VOLUME_NAME),
            "${MediaStore.Video.Media._ID} = ?",
            arrayOf(mediaId.toString()),
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return null
            val relPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH))
                ?.trimEnd('/') ?: return null
            val volName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.VOLUME_NAME))
            relPath to volName
        } ?: return null

        val documentRoot = resolveDocumentRoot(context, volumeName) ?: return null
        return documentRoot to relativePath
    }

    private fun resolveDocumentRoot(context: Context, mediaStoreVolumeName: String?): String? {
        if (
            mediaStoreVolumeName == null
            || mediaStoreVolumeName == MediaStore.VOLUME_EXTERNAL_PRIMARY
            || Build.VERSION.SDK_INT < Build.VERSION_CODES.R
        ) {
            return "primary"
        }

        val storageManager = context.getSystemService(StorageManager::class.java) ?: return null

        val matchingVolume = storageManager.storageVolumes.firstOrNull { volume ->
            volume.mediaStoreVolumeName == mediaStoreVolumeName
        } ?: return null

        return if (matchingVolume.isPrimary) "primary" else matchingVolume.uuid
    }

    private fun launchExplorer(context: Context, targetUri: Uri?) {

        if (targetUri != null) {
            val preciseIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(targetUri, DocumentsContract.Document.MIME_TYPE_DIR)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(preciseIntent)
                return
            } catch (e: Exception) {
                Trace.error(tag = TAG, message = "Precise folder navigation failed, falling back", e)
            }
        }

        val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            setDataAndType(
                DocumentsContract.buildRootsUri("com.android.externalstorage.documents"),
                DocumentsContract.Root.MIME_TYPE_ITEM,
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(fallbackIntent)
        } catch (e: ActivityNotFoundException) {
            Trace.error(tag = TAG, message = "No file explorer available at all", e)
        }
    }

}