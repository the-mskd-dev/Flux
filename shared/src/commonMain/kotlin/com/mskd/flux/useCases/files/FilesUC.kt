package com.mskd.flux.useCases.files

import com.mskd.flux.model.domain.files.UserFile
import java.io.File

interface FilesUC {

    suspend fun getFiles() : List<UserFile>

    suspend fun filterExistingFiles(files: List<UserFile>) : List<UserFile>

    suspend fun getSubtitlesFor(file: UserFile) : File?

}