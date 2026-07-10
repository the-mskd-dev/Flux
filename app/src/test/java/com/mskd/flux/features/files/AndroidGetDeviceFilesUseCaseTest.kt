package com.mskd.flux.features.files

import com.mskd.flux.features.files.data.usecase.AndroidFilterExistingFilesUseCase
import com.mskd.flux.features.files.data.usecase.AndroidGetDeviceFilesUseCase
import com.mskd.flux.mockups.FilesMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.property.Arb
import io.kotest.property.arbitrary.subsequence
import io.kotest.property.checkAll

class AndroidGetDeviceFilesUseCaseTest: FunSpec ({

    test("get files from multiple sources") {

        checkAll(
            Arb.subsequence(FilesMockups.localFiles),
            Arb.subsequence(FilesMockups.safFiles)
        ) { mediaStoreFiles, safFiles ->

            val mediaStoreDataSource = FakeFilesDataSource(availableFiles = mediaStoreFiles)
            val safDataSource = FakeFilesDataSource(availableFiles = safFiles)

            val useCase = AndroidGetDeviceFilesUseCase(
                mediaStore = mediaStoreDataSource,
                saf = safDataSource
            )

            val result = useCase()

            val expected = mediaStoreFiles + safFiles

            result shouldContainExactlyInAnyOrder expected

        }

    }
})