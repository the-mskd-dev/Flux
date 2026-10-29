package com.mskd.flux.system

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.report.Trace

class AndroidFilesLauncher(private val context: Context) : FilesLauncher {

    override fun open(file: UserFile) {
        val targetUri = resolveTargetUri(file)
        launchExplorer(targetUri)
    }

    override fun openDownloadedImages() {
        launchExplorer(DocumentsContract.buildDocumentUri(EXTERNAL_STORAGE_AUTHORITY, "primary:$DOWNLOADED_IMAGES_PATH"))
    }

    private fun resolveTargetUri(file: UserFile): Uri? {
        return when (file.source) {
            FileSource.SAF -> file.path.toUri()

            FileSource.LOCAL -> {
                val folder = file.realPath.substringBeforeLast('/', missingDelimiterValue = "")
                if (folder.isEmpty()) return null

                DocumentsContract.buildDocumentUri(
                    EXTERNAL_STORAGE_AUTHORITY,
                    "primary:$folder",
                )
            }
        }
    }

    private fun launchExplorer(targetUri: Uri?) {

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
                Trace.error(tag = "FilesLauncher", message = "Precise folder navigation failed, falling back", e)
            }
        }

        val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            setDataAndType(
                DocumentsContract.buildRootsUri(EXTERNAL_STORAGE_AUTHORITY),
                DocumentsContract.Root.MIME_TYPE_ITEM,
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(fallbackIntent)
        } catch (e: ActivityNotFoundException) {
            Trace.error(tag = "FilesLauncher", message = "No file explorer available at all", e)
        }
    }

    private companion object {
        const val EXTERNAL_STORAGE_AUTHORITY = "com.android.externalstorage.documents"
    }

}
