package com.mskd.flux.features.files

import com.mskd.flux.features.files.data.usecase.AndroidFilterExistingFilesUseCase
import com.mskd.flux.mockups.FilesMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.subsequence
import io.kotest.property.checkAll

class AndroidFilterExistingFilesUseCaseTest : FunSpec ({

    test("result should be the union between mediastore and saf") {

        checkAll(
            Arb.subsequence(FilesMockups.localFiles),
            Arb.subsequence(FilesMockups.safFiles)
        ) { mediaStoreFiles, safFiles ->

            val mediaStoreDataSource = FakeFilesDataSource(availableFiles = mediaStoreFiles)
            val safDataSource = FakeFilesDataSource(availableFiles = safFiles)

            val useCase = AndroidFilterExistingFilesUseCase(
                mediaStore = mediaStoreDataSource,
                saf = safDataSource
            )

            val inputFiles = FilesMockups.localFiles + FilesMockups.safFiles
            val result = useCase(files = inputFiles)

            val expected = (mediaStoreFiles + safFiles)

            result shouldContainExactlyInAnyOrder expected

        }
    }

    test("no file, no result") {
        val mediaStoreDataSource = FakeFilesDataSource(availableFiles = emptyList())
        val safDataSource = FakeFilesDataSource(availableFiles = emptyList())

        val useCase = AndroidFilterExistingFilesUseCase(
            mediaStore = mediaStoreDataSource,
            saf = safDataSource
        )

        val result = useCase(files = emptyList())

        result shouldBe emptyList()
    }

})