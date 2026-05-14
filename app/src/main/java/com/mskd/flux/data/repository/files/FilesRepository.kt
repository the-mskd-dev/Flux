package com.mskd.flux.data.repository.files

import android.net.Uri
import com.mskd.flux.model.UserFile

interface FilesRepository {

    suspend fun getFiles() : List<UserFile>

    suspend fun filterExistingFiles(files: List<UserFile>) : List<UserFile>

    suspend fun getSubtitlesFor(file: UserFile) : Uri?

}