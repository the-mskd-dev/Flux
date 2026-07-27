package com.mskd.flux.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.net.toUri
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile

object FileUtils {

    fun openFileExplorer(context: Context, file: UserFile) {

        val targetUri = resolveTargetUri(
            context = context,
            file = file,
        )

        launchExplorer(context, targetUri)

    }

    private fun resolveTargetUri(
        context: Context,
        file: UserFile,
    ): Uri? {
        return when (file.source) {
            FileSource.SAF -> file.path.toUri()

            FileSource.LOCAL -> {
                val folder = file.realPath.takeIf { it.isNotBlank() }
                    ?.substringBeforeLast('/', missingDelimiterValue = "")
                    ?: fetchRelativeFolderFromMediaStore(context, file.path)
                buildDocumentUriForPrimaryFolder(folder)
            }
        }
    }

    private fun fetchRelativeFolderFromMediaStore(context: Context, path: String): String? {
        val mediaId = path.toUri().lastPathSegment?.toLongOrNull() ?: return null

        return context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Video.Media.RELATIVE_PATH),
            "${MediaStore.Video.Media._ID} = ?",
            arrayOf(mediaId.toString()),
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH))
                    ?.trimEnd('/')
            } else null
        }
    }

    private fun buildDocumentUriForPrimaryFolder(folder: String?): Uri? {
        if (folder.isNullOrEmpty()) return null
        return DocumentsContract.buildDocumentUri(
            "com.android.externalstorage.documents",
            "primary:$folder",
        )
    }

    private fun launchExplorer(context: Context, targetUri: Uri?) {

        if (targetUri == null) {
            Trace.error(tag = "FileUtils", message = "Unable to resolve folder URI")
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(targetUri, DocumentsContract.Document.MIME_TYPE_DIR)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Trace.error(tag = "FileUtils", message = "No file explorer available", e)
        }

    }

}