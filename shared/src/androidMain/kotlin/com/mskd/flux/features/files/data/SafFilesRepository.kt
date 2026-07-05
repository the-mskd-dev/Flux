package com.mskd.flux.features.files.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import com.mskd.flux.core.domain.model.files.FileSource
import com.mskd.flux.core.domain.model.files.UserFile
import com.mskd.flux.features.files.domain.repository.FilesRepository
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SafFilesRepository(
    private val context: Context,
    private val sources: SourcesRepository
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

        // Étape 2 : Le SAF est basé sur des structures d'arbres. Pour optimiser,
        // on regroupe nos vérifications. La méthode la plus fiable en SAF Document Tree
        // est de tenter d'ouvrir un curseur sur l'URI exacte du document.
        for (file in safFiles) {
            val fileUri = file.path.toUri()
            var exists = false

            try {
                // On demande uniquement l'ID pour minimiser la data transférée par le curseur
                context.contentResolver.query(
                    fileUri,
                    arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    // Si le fichier existe et est accessible, le curseur contiendra au moins une ligne
                    if (cursor.moveToFirst()) {
                        exists = true
                    }
                }
            } catch (e: Exception) {
                // Si le fichier a été supprimé ou que les permissions ont expiré,
                // le ContentProvider lèvera une SecurityException ou une FileNotFoundException.
                Trace.error(TAG, "File no longer accessible or missing: ${file.name}", e)
            }

            if (exists) {
                existingFiles.add(file)
            } else {
                missingFiles.add(file)
            }
        }

        // Étape 3 : Logging de production identique à ton implémentation MediaStore
        if (missingFiles.isNotEmpty()) {
            Trace.info(TAG, "${missingFiles.size} SAF file(s) not found or revoked")
            missingFiles.forEach { Trace.info(TAG, it.name) }
        }

        existingFiles
    }

    override suspend fun getSubtitlesFor(file: UserFile): File? {
        return null // TODO
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
                            source = FileSource.SAF
                        )
                    }
                }
            }
        }
    }

}