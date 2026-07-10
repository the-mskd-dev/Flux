package com.mskd.flux.features.files

import com.mskd.flux.features.files.data.usecase.AndroidGetDeviceFilesUseCase
import com.mskd.flux.features.files.data.usecase.AndroidGetSubtitlesUseCase
import com.mskd.flux.features.files.fake.FakeFilesDataSource
import com.mskd.flux.mockups.FilesMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.file
import io.kotest.property.arbitrary.subsequence
import io.kotest.property.checkAll

class AndroidGetSubtitlesUseCaseTest: FunSpec ({

    test("get subtitle from file if it exist") {

        checkAll(
            Arb.element(FilesMockups.mediaStoreFiles + FilesMockups.safFiles),
        ) { file ->

            val mediaStoreDataSource = FakeFilesDataSource(availableFiles = FilesMockups.mediaStoreFiles)
            val safDataSource = FakeFilesDataSource(availableFiles = FilesMockups.safFiles)

            val useCase = AndroidGetSubtitlesUseCase(
                mediaStore = mediaStoreDataSource,
                saf = safDataSource
            )

            val result = useCase(file)


            result shouldBe FilesMockups.subtitles.find { it.equals(file.name, ignoreCase = true) }

        }

    }

})