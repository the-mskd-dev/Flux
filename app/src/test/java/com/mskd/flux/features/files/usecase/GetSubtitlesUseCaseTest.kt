package com.mskd.flux.features.files.usecase

import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCaseImpl
import com.mskd.flux.features.files.fake.FakeFilesDataSource
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import com.mskd.flux.mockups.FilesMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.mockk

class GetSubtitlesUseCaseTest: FunSpec ({

    lateinit var sourcesProvider: SourcesProvider

    beforeTest {

        sourcesProvider = mockk(relaxed = true)

    }

    test("get subtitle from file if it exist") {

        checkAll(
            iterations = 50,
            Arb.element(FilesMockups.mediaStoreFiles + FilesMockups.safFiles),
        ) { file ->

            val mediaStoreDataSource = FakeFilesDataSource(availableFiles = FilesMockups.mediaStoreFiles, root = "mediaStore/")
            val safDataSource = FakeFilesDataSource(availableFiles = FilesMockups.safFiles, root = "saf/")
            coEvery { sourcesProvider.getSources() } returns listOf(mediaStoreDataSource, safDataSource)

            val useCase = GetSubtitlesUseCaseImpl(sourcesProvider = sourcesProvider)

            val result = useCase(file)

            result shouldBe FilesMockups.subtitles.find { it.equals(file.name, ignoreCase = true) }

        }

    }

    test("no subtitle for a file outside of the active sources") {

        // Given
        val safDataSource = FakeFilesDataSource(availableFiles = FilesMockups.safFiles, root = "saf/")
        coEvery { sourcesProvider.getSources() } returns listOf(safDataSource)

        val useCase = GetSubtitlesUseCaseImpl(sourcesProvider = sourcesProvider)

        // When
        val result = useCase(FilesMockups.mediaStoreFiles.first())

        // Then
        result shouldBe null

    }

})
