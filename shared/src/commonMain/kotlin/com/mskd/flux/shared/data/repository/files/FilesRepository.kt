package com.mskd.flux.shared.data.repository.files

import com.mskd.flux.shared.model.UserFile
import java.io.File

interface FilesRepository {

    suspend fun getFiles() : List<UserFile>

    suspend fun filterExistingFiles(files: List<UserFile>) : List<UserFile>

    suspend fun getSubtitlesFor(file: UserFile) : File?

}