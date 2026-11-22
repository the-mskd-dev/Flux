package com.mskd.flux.features.files.usecase

import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.model.ExternalSubtitle
import com.mskd.flux.features.files.domain.model.SubtitleCandidate
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCaseImpl
import com.mskd.flux.features.files.fake.FakeFilesDataSource
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import com.mskd.flux.mockups.FilesMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.coEvery
import io.mockk.mockk

class GetSubtitlesUseCaseTest : FunSpec({

    lateinit var sourcesProvider: SourcesProvider

    beforeTest {
        sourcesProvider = mockk(relaxed = true)
    }

    test("returns all matching multi-sidecar subtitles ordered by priority from the owning source") {
        val episode = UserFile(
            name = "Episode01.mkv",
            addedDateTime = 0L,
            path = "saf/Episode01.mkv",
            realPath = "saf/Episode01.mkv",
            source = FileSource.SAF,
        )
        val safDataSource = FakeFilesDataSource(
            availableFiles = listOf(episode),
            root = "saf/",
            subtitleCandidates = listOf(
                SubtitleCandidate("saf/Episode01.fr.srt", "Episode01.fr.srt"),
                SubtitleCandidate("saf/Episode010.ass", "Episode010.ass"),
                SubtitleCandidate("saf/Episode01.fr.forced.ass", "Episode01.fr.forced.ass"),
                SubtitleCandidate("saf/Episode01.fr.ass", "Episode01.fr.ass"),
                SubtitleCandidate("saf/Episode01.ass", "Episode01.ass"),
            )
        )
        val mediaStoreDataSource = FakeFilesDataSource(
            availableFiles = FilesMockups.mediaStoreFiles,
            root = "mediaStore/",
            subtitleCandidates = listOf(
                SubtitleCandidate("mediaStore/Episode01.en.ass", "Episode01.en.ass"),
            )
        )
        coEvery { sourcesProvider.getSources() } returns listOf(mediaStoreDataSource, safDataSource)

        val useCase = GetSubtitlesUseCaseImpl(sourcesProvider = sourcesProvider)

        val result = useCase(episode)

        result shouldContainExactly listOf(
            ExternalSubtitle(
                uri = "saf/Episode01.ass",
                fileName = "Episode01.ass",
                extension = "ass",
                language = null,
                tags = emptyList(),
            ),
            ExternalSubtitle(
                uri = "saf/Episode01.fr.ass",
                fileName = "Episode01.fr.ass",
                extension = "ass",
                language = "fr",
                tags = emptyList(),
            ),
            ExternalSubtitle(
                uri = "saf/Episode01.fr.forced.ass",
                fileName = "Episode01.fr.forced.ass",
                extension = "ass",
                language = "fr",
                tags = listOf("forced"),
            ),
            ExternalSubtitle(
                uri = "saf/Episode01.fr.srt",
                fileName = "Episode01.fr.srt",
                extension = "srt",
                language = "fr",
                tags = emptyList(),
            ),
        )
    }

    test("returns empty list for a file outside of active sources") {
        val safDataSource = FakeFilesDataSource(availableFiles = FilesMockups.safFiles, root = "saf/")
        coEvery { sourcesProvider.getSources() } returns listOf(safDataSource)

        val useCase = GetSubtitlesUseCaseImpl(sourcesProvider = sourcesProvider)

        val result = useCase(FilesMockups.mediaStoreFiles.first())

        result.shouldBeEmpty()
    }
})
