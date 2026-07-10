package com.mskd.flux.features.files.fake

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.mockups.FilesMockups

internal class FakeFilesDataSource(
    private val availableFiles: List<UserFile>
) : FilesDataSource {

    override suspend fun getFiles(): List<UserFile> {
        return availableFiles
    }

    override suspend fun filterExistingFiles(files: List<UserFile>): List<UserFile> {
        return files.filter { file -> availableFiles.any { it.path == file.path } }
    }

    override suspend fun getSubtitlesFor(file: UserFile): String? {
        return FilesMockups.subtitles.find { it.equals(file.name, ignoreCase = true) }
    }

}