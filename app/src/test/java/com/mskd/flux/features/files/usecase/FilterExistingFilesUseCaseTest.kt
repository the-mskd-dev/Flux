package com.mskd.flux.features.files.usecase

import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCaseImpl
import com.mskd.flux.features.files.fake.FakeFilesDataSource
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import com.mskd.flux.mockups.FilesMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.subsequence
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.mockk

class FilterExistingFilesUseCaseTest: FunSpec ({

    lateinit var sourcesProvider: SourcesProvider

    beforeTest {

        sourcesProvider = mockk(relaxed = true)

    }

    test("result should be the union between mediastore and saf") {

        checkAll(
            Arb.subsequence(FilesMockups.mediaStoreFiles),
            Arb.subsequence(FilesMockups.safFiles)
        ) { mediaStoreFiles, safFiles ->

            val mediaStoreDataSource = FakeFilesDataSource(availableFiles = mediaStoreFiles)
            val safDataSource = FakeFilesDataSource(availableFiles = safFiles)
            coEvery { sourcesProvider.getSources() } returns listOf(mediaStoreDataSource, safDataSource)

            val useCase = FilterExistingFilesUseCaseImpl(sourcesProvider = sourcesProvider)

            val inputFiles = FilesMockups.mediaStoreFiles + FilesMockups.safFiles
            val result = useCase(files = inputFiles)

            val expected = mediaStoreFiles + safFiles

            result shouldContainExactlyInAnyOrder expected

        }
    }

    test("no file, no result") {
        val mediaStoreDataSource = FakeFilesDataSource(availableFiles = emptyList())
        val safDataSource = FakeFilesDataSource(availableFiles = emptyList())
        coEvery { sourcesProvider.getSources() } returns listOf(mediaStoreDataSource, safDataSource)

        val useCase = FilterExistingFilesUseCaseImpl(sourcesProvider = sourcesProvider)

        val result = useCase(files = emptyList())

        result shouldBe emptyList()
    }

})