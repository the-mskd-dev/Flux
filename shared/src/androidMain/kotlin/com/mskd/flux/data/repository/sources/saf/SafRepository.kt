package com.mskd.flux.data.repository.sources.saf

import com.mskd.flux.data.repository.sources.SourcesRepository
import com.mskd.flux.model.domain.files.UserFile
import java.io.File

class SafRepository : SourcesRepository {

    override suspend fun getFiles(): List<UserFile> {
        TODO("Not yet implemented")
    }

    override suspend fun filterExistingFiles(files: List<UserFile>): List<UserFile> {
        TODO("Not yet implemented")
    }

    override suspend fun getSubtitlesFor(file: UserFile): File? {
        TODO("Not yet implemented")
    }

}