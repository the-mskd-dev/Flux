package com.mskd.flux.features.files.data.datasource

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.data.FileExtensions
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.files.domain.model.ExternalSubtitle
import com.mskd.flux.features.files.domain.model.ExternalSubtitleMatcher
import com.mskd.flux.features.files.domain.model.SubtitleCandidate
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.report.Trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Source of a custom folder: available as long as the folder can be accessed.
 */
class SafFilesDataSource(
    private val context: Context,
    private val folder: UserFolder,
) : FilesDataSource {

    companion object {
        private const val TAG = "SafFilesDataSource"
        private val PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )
    }

    override suspend fun isAvailable(): Boolean = folder.isAvailable

    override fun contains(file: UserFile): Boolean =
        file.source == FileSource.SAF && file.path.startsWith(folder.path)

    override suspend fun getFiles(): List<UserFile> = withContext(Dispatchers.IO) {
        getFilesFromFolder(folder = folder)
    }

    override suspend fun getSubtitlesFor(file: UserFile): List<ExternalSubtitle> = withContext(Dispatchers.IO) {
        if (!contains(file)) return@withContext emptyList()

        val videoUri = file.path.toUri()

        try {
            val parentDocumentId = file.parentDocId
            if (parentDocumentId.isNullOrEmpty()) return@withContext emptyList()

            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(videoUri, parentDocumentId)
            val candidates = mutableListOf<SubtitleCandidate>()

            // Targeted query on the parent folder
            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                ),
                null, null, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

                while (cursor.moveToNext()) {
                    val mime = if (mimeCol >= 0) cursor.getString(mimeCol) else null
                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) continue

                    val currentId = cursor.getString(idCol) ?: continue
                    val currentName = cursor.getString(nameCol) ?: continue
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(videoUri, currentId)
                    candidates += SubtitleCandidate(uri = docUri.toString(), fileName = currentName)
                }
            }

            ExternalSubtitleMatcher.matchAll(userFileName = file.name, candidates = candidates)
        } catch (e: Exception) {
            Trace.error(TAG, "Failed to resolve SAF subtitles for ${file.name}", e)
            emptyList()
        }
    }

    private fun getFilesFromFolder(folder: UserFolder): List<UserFile> {
        val treeUri = folder.path.toUri()
        val files = mutableListOf<UserFile>()
        try {
            val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
            traverse(
                treeUri = treeUri,
                parentDocId = rootDocId,
                relativePath = "",
                acc = files
            )
        } catch (e: Exception) {
            Trace.error(TAG, "Fail to traverse tree $treeUri", e)
        }
        return files
    }

    private fun traverse(
        treeUri: Uri,
        parentDocId: String,
        relativePath: String,
        acc: MutableList<UserFile>
    ) {

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)

        val cursor = context.contentResolver.query(
            childrenUri,
            PROJECTION,
            null,
            null,
            null
        ) ?: return

        cursor.use {
            val idCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val dateCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

            while (it.moveToNext()) {
                val docId = it.getString(idCol)
                val name = it.getString(nameCol)
                val mime = it.getString(mimeCol)
                val lastModified = it.getLong(dateCol)
                val childRelativePath = "$relativePath$name"

                if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                    traverse(
                        treeUri = treeUri,
                        parentDocId = docId,
                        relativePath = "$childRelativePath/",
                        acc = acc)
                } else {
                    val extension = name.substringAfterLast('.', "").lowercase()
                    if (extension in FileExtensions.VIDEOS) {
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        acc += UserFile(
                            name = name,
                            addedDateTime = lastModified,
                            path = docUri.toString(),
                            realPath = childRelativePath,
                            source = FileSource.SAF,
                            parentDocId = parentDocId
                        )
                    }
                }
            }
        }
    }

}