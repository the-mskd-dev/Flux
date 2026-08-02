package com.mskd.flux.features.files.usecase

import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCaseImpl
import com.mskd.flux.features.files.fake.FakeFilesDataSource
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import com.mskd.flux.mockups.FilesMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.property.Arb
import io.kotest.property.arbitrary.subsequence
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.mockk

class GetDeviceFilesUseCaseTest: FunSpec ({

    lateinit var sourcesProvider: SourcesProvider

    beforeTest {

        sourcesProvider = mockk(relaxed = true)

    }

    test("get files from multiple sources") {

        checkAll(
            Arb.subsequence(FilesMockups.mediaStoreFiles),
            Arb.subsequence(FilesMockups.safFiles)
        ) { mediaStoreFiles, safFiles ->

            val mediaStoreDataSource = FakeFilesDataSource(availableFiles = mediaStoreFiles)
            val safDataSource = FakeFilesDataSource(availableFiles = safFiles)
            coEvery { sourcesProvider.getSources() } returns listOf(mediaStoreDataSource, safDataSource)

            val useCase = GetDeviceFilesUseCaseImpl(sourcesProvider = sourcesProvider)

            val result = useCase()

            val expected = mediaStoreFiles + safFiles

            result shouldContainExactlyInAnyOrder expected

        }

    }

})