package com.mskd.flux.features.files.fake

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.files.domain.model.ExternalSubtitle
import com.mskd.flux.features.files.domain.model.ExternalSubtitleMatcher
import com.mskd.flux.features.files.domain.model.SubtitleCandidate

/**
 * In-memory Source: owns the files whose path starts with [root].
 * An unavailable Source cannot be read, but its files are still considered existing.
 */
internal class FakeFilesDataSource(
    var availableFiles: List<UserFile>,
    var available: Boolean = true,
    private val root: String = "",
    var subtitleCandidates: List<SubtitleCandidate> = emptyList(),
) : FilesDataSource {

    override suspend fun isAvailable(): Boolean = available

    override fun contains(file: UserFile): Boolean = file.path.startsWith(root)

    override suspend fun getFiles(): List<UserFile> {
        return if (available) availableFiles else emptyList()
    }

    override suspend fun getSubtitlesFor(file: UserFile): List<ExternalSubtitle> {
        if (!contains(file)) return emptyList()
        return ExternalSubtitleMatcher.matchAll(
            userFileName = file.name,
            candidates = subtitleCandidates,
        )
    }

}
