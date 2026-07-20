package com.mskd.flux.features.files.usecase

import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCaseImpl
import com.mskd.flux.features.files.fake.FakeFilesDataSource
import com.mskd.flux.mockups.FilesMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll

class GetSubtitlesUseCaseTest: FunSpec ({

    test("get subtitle from file if it exist") {

        checkAll(
            Arb.element(FilesMockups.mediaStoreFiles + FilesMockups.safFiles),
        ) { file ->

            val mediaStoreDataSource = FakeFilesDataSource(availableFiles = FilesMockups.mediaStoreFiles)
            val safDataSource = FakeFilesDataSource(availableFiles = FilesMockups.safFiles)

            val useCase = GetSubtitlesUseCaseImpl(
                sources = mapOf(
                    FileSource.LOCAL to mediaStoreDataSource,
                    FileSource.SAF to safDataSource,
                )
            )

            val result = useCase(file)


            result shouldBe FilesMockups.subtitles.find { it.equals(file.name, ignoreCase = true) }

        }

    }

})