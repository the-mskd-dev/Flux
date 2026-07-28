package com.mskd.flux.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile

object FileUtils {

    fun openFileExplorer(context: Context, file: UserFile) {
        val targetUri = resolveTargetUri(file)
        launchExplorer(context, targetUri)
    }

    private fun resolveTargetUri(file: UserFile): Uri? {
        return when (file.source) {
            FileSource.SAF -> file.path.toUri()

            FileSource.LOCAL -> {
                val folder = file.realPath.substringBeforeLast('/', missingDelimiterValue = "")
                if (folder.isEmpty()) return null

                DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    "primary:$folder",
                )
            }
        }
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
                Trace.error(tag = "FileUtils", message = "Precise folder navigation failed, falling back", e)
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
            Trace.error(tag = "FileUtils", message = "No file explorer available at all", e)
        }
    }

}