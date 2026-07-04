package com.mskd.flux.features.files.domain.repository

import com.mskd.flux.core.domain.model.files.UserFile
import java.io.File

interface FilesRepository {
    suspend fun getFiles() : List<UserFile>
    suspend fun filterExistingFiles(files: List<UserFile>) : List<UserFile>
    suspend fun getSubtitlesFor(file: UserFile) : File?
}