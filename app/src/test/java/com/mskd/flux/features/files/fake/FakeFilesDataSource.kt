package com.mskd.flux.features.files.fake

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.mockups.FilesMockups

/**
 * In-memory Source: owns the files whose path starts with [root].
 * An unavailable Source cannot be read, but its files are still considered existing.
 */
internal class FakeFilesDataSource(
    var availableFiles: List<UserFile>,
    var available: Boolean = true,
    private val root: String = "",
) : FilesDataSource {

    override suspend fun isAvailable(): Boolean = available

    override fun contains(file: UserFile): Boolean = file.path.startsWith(root)

    override suspend fun getFiles(): List<UserFile> {
        return if (available) availableFiles else emptyList()
    }

    override suspend fun getSubtitlesFor(file: UserFile): String? {
        return FilesMockups.subtitles.find { it.equals(file.name, ignoreCase = true) }
    }

}
