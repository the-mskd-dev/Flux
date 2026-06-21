package com.mskd.flux.data.repository.files

import com.mskd.flux.model.UserFile
import java.io.File

interface FilesRepository {

    suspend fun getFiles() : List<UserFile>

    suspend fun filterExistingFiles(files: List<UserFile>) : List<UserFile>

    suspend fun getSubtitlesFor(file: UserFile) : File?

}