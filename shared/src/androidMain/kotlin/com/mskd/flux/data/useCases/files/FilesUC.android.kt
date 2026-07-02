package com.mskd.flux.data.useCases.files

import com.mskd.flux.data.repository.sources.SourcesFilesRepository
import com.mskd.flux.model.domain.files.UserFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FilesUCImpl(
    private val mediaStoreRepository: SourcesFilesRepository
) : FilesUC {

    companion object {
        const val TAG = "FilesUCImpl"
    }

    override suspend fun getFiles(): List<UserFile> {

        val files = mediaStoreRepository.getFiles()

        return files

    }

    override suspend fun filterExistingFiles(files: List<UserFile>): List<UserFile> = withContext(Dispatchers.IO) {

        val existingFiles = mediaStoreRepository.filterExistingFiles(files = files)

        existingFiles

    }

    override suspend fun getSubtitlesFor(file: UserFile): File? = withContext(Dispatchers.IO) {

        mediaStoreRepository.getSubtitlesFor(file = file)

    }

}