package com.mskd.flux.features.sources.data

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.validator.UserFolderValidator
import com.mskd.flux.utils.Trace

class AndroidUserFolderValidator(
    private val context: Context
) : UserFolderValidator {

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