package com.mskd.flux.data.repository.sources

import com.mskd.flux.model.domain.files.UserFile
import java.io.File

interface SourcesRepository {
    suspend fun getFiles() : List<UserFile>
    suspend fun filterExistingFiles(files: List<UserFile>) : List<UserFile>
    suspend fun getSubtitlesFor(file: UserFile) : File?
}