package com.mskd.flux.shared.data.repository.files

import android.net.Uri
import com.mskd.flux.shared.model.UserFile

interface FilesRepository {

    suspend fun getFiles() : List<UserFile>

    suspend fun filterExistingFiles(files: List<UserFile>) : List<UserFile>

    suspend fun getSubtitlesFor(file: UserFile) : Uri?

}