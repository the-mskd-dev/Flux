package com.mskd.flux.features.sources.data

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.mskd.flux.features.sources.domain.validator.UserFolderValidator
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidUserFolderValidator(
    private val context: Context
) : UserFolderValidator {

    override suspend fun isFolderAvailable(path: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val uri = path.toUri()
            val documentFile = DocumentFile.fromTreeUri(context, uri)

            documentFile?.exists() == true && documentFile.canRead()

        } catch (e: Exception) {
            Trace.error(
                tag = "AndroidUserFolderValidator",
                message = "Folder at path `$path` isn't available",
                throwable = e
            )
            false
        }
    }

}