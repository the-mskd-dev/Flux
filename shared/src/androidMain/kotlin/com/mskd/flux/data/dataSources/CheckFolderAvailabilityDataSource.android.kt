package com.mskd.flux.data.dataSources

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.mskd.flux.model.domain.files.UserFolder
import com.mskd.flux.utils.Trace

class AndroidCheckFolderAvailabilityDataSource(
    private val context: Context
) : CheckFolderAvailabilityDataSource {

    override suspend fun isFolderAvailable(path: String): UserFolder.Status {
        return try {
            val uri = path.toUri()
            val documentFile = DocumentFile.fromTreeUri(context, uri)

            if (documentFile?.exists() == true && documentFile.canRead()) {
                UserFolder.Status.AVAILABLE
            } else {
                UserFolder.Status.MISSING
            }
        } catch (e: Exception) {
            Trace.error(
                tag = "CheckFolderAvailabilityUseCase",
                message = "Folder (${path.toUri().lastPathSegment} isn't available",
                throwable = e
            )
            UserFolder.Status.MISSING
        }
    }

}