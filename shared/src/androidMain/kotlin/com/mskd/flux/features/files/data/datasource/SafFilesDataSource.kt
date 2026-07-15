package com.mskd.flux.features.files.data.datasource

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.data.FileExtensions
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SafFilesDataSource(
    private val context: Context,
    private val sources: SourcesRepository,
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

    override suspend fun getFiles(): List<UserFile> = withContext(Dispatchers.IO) {
        val folders = sources.getFolders()

        folders.flatMap { folder ->
            getFilesFromFolder(folder = folder)
        }
    }

    override suspend fun filterExistingFiles(files: List<UserFile>): List<UserFile> = withContext(Dispatchers.IO) {

        val safFiles = files.filter { it.source == FileSource.SAF }
        if (safFiles.isEmpty()) return@withContext emptyList()

        val existingFiles = mutableListOf<UserFile>()
        val missingFiles = mutableListOf<UserFile>()

        val availableFolders = sources.getFolders().filter { it.isAvailable }

        val (fromAvailableFolders, fromUnavailableFolders) = safFiles.partition { file ->
            availableFolders.any { file.path.startsWith(it.path) }
        }

        existingFiles.addAll(fromUnavailableFolders)

        for (file in fromAvailableFolders) {
            val fileUri = file.path.toUri()
            var exists = false

            try {
                context.contentResolver.query(
                    fileUri,
                    arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        exists = true
                    }
                }
            } catch (e: Exception) {
                Trace.error(TAG, "File no longer accessible or missing: ${file.name}", e)
            }

            if (exists) {
                existingFiles.add(file)
            } else {
                missingFiles.add(file)
            }
        }

        if (missingFiles.isNotEmpty()) {
            Trace.info(TAG, "${missingFiles.size} SAF file(s) not found or revoked")
            missingFiles.forEach { Trace.info(TAG, it.name) }
        }

        existingFiles
    }

    override suspend fun getSubtitlesFor(file: UserFile): String? = withContext(Dispatchers.IO) {
        if (file.source != FileSource.SAF) return@withContext null

        val videoUri = file.path.toUri()

        // Cleanly extract the video name without its extension
        val videoNameWithoutExtension = file.name.substringAfterLast('/').substringBeforeLast('.')

        try {

            // Reconstruct the parent folder ID by removing the last encoded segment
            val parentDocumentId = file.parentDocId
            if (parentDocumentId.isNullOrEmpty()) return@withContext null

            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(videoUri, parentDocumentId)

            val subtitleExtensions = FileExtensions.SUBTITLES
            var targetSubtitleUri: Uri? = null

            // Targeted query on the parent folder
            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                ),
                null, null, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val currentName = cursor.getString(nameCol)
                    val currentExt = currentName.substringAfterLast('.', "").lowercase()
                    val currentBaseName = currentName.substringBeforeLast('.')

                    // Exact name match (case-insensitive) + valid extension
                    if (currentBaseName.equals(videoNameWithoutExtension, ignoreCase = true) && currentExt in subtitleExtensions) {
                        val currentId = cursor.getString(idCol)
                        targetSubtitleUri = DocumentsContract.buildDocumentUriUsingTree(videoUri, currentId)
                        break // Found, stop the cursor immediately.
                    }
                }
            }

            // Return the SAF URI directly as a String
            return@withContext targetSubtitleUri?.toString()

        } catch (e: Exception) {
            Trace.error(TAG, "Failed to resolve SAF subtitles for ${file.name}", e)
            null
        }

    }

    private fun getFilesFromFolder(folder: UserFolder): List<UserFile> {
        val treeUri = folder.path.toUri()
        val files = mutableListOf<UserFile>()
        try {
            val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
            traverse(treeUri, rootDocId, files)
        } catch (e: Exception) {
            Trace.error(TAG, "Fail to traverse tree $treeUri", e)
        }
        return files
    }

    private fun traverse(treeUri: Uri, parentDocId: String, acc: MutableList<UserFile>) {

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

                if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                    traverse(treeUri, docId, acc)
                } else {
                    val extension = name.substringAfterLast('.', "").lowercase()
                    if (extension in FileExtensions.VIDEOS) {
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        acc += UserFile(
                            name = name,
                            addedDateTime = lastModified,
                            path = docUri.toString(),
                            source = FileSource.SAF,
                            parentDocId = parentDocId
                        )
                    }
                }
            }
        }
    }

}