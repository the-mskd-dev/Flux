package com.mskd.flux.data.repository.sources.saf

import com.mskd.flux.data.dataSources.CheckFolderAvailabilityDataSource
import com.mskd.flux.data.repository.ddb.sources.SourcesRepository
import com.mskd.flux.data.repository.sources.SourcesFilesRepository
import com.mskd.flux.model.domain.files.UserFile
import java.io.File

class SafFilesRepository(
    private val sourcesRepository: SourcesRepository,
) : SourcesFilesRepository {

    companion object {
        private const val TAG = "SafFilesRepository"
        private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "avi", "mov", "webm", "ts", "m4v")
        private val SUBTITLE_EXTENSIONS = setOf("srt", "vtt", "ass", "ssa")
    }

    override suspend fun getFiles(): List<UserFile> {
        val folders = sourcesRepository.getFolders()
        return emptyList()
    }

    override suspend fun filterExistingFiles(files: List<UserFile>): List<UserFile> {
        TODO("Not yet implemented")
    }

    override suspend fun getSubtitlesFor(file: UserFile): File? {
        TODO("Not yet implemented")
    }

}