package com.mskd.flux.features.files.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import com.mskd.flux.features.files.domain.repository.FilesRepository
import com.mskd.flux.features.sources.data.AndroidUserFolderValidator
import com.mskd.flux.features.sources.data.dataSource.SourcesDataSource
import com.mskd.flux.features.sources.data.model.toDomain
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.model.domain.files.FileSource
import com.mskd.flux.model.domain.files.UserFile
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SafFilesRepository(
    private val context: Context,
    private val dataSource: SourcesDataSource,
    private val folderValidator: AndroidUserFolderValidator
) : FilesRepository {

    companion object {
        private const val TAG = "SafFilesRepository"
        private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "avi", "mov", "webm", "ts", "m4v")

        private val PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )
    }

    override suspend fun getFiles(): List<UserFile> = withContext(Dispatchers.IO) {
        val availableFolders = dataSource.getFolders().map {
            val status = folderValidator.isFolderAvailable(path = it.path)
            it.toDomain(status = status)
        }

        availableFolders.flatMap { folder ->
            getFilesFromFolder(folder = folder)
        }
    }

    override suspend fun filterExistingFiles(files: List<UserFile>): List<UserFile> {
        TODO("Not yet implemented")
    }

    override suspend fun getSubtitlesFor(file: UserFile): File? {
        TODO("Not yet implemented")
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
                    if (extension in VIDEO_EXTENSIONS) {
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        acc += UserFile(
                            name = name,
                            addedDateTime = lastModified,
                            path = docUri.toString(),
                            source = FileSource.LOCAL
                        )
                    }
                }
            }
        }
    }

}