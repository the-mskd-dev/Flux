package com.mskd.flux.features.sources.usecase

import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.usecase.DeleteSourceUseCase
import com.mskd.flux.features.sources.fake.FakeUserFolders
import io.kotest.core.spec.style.FunSpec
import io.mockk.coVerify
import io.mockk.mockk

class DeleteSourceUseCaseTest : FunSpec ({

    test("delete source through use case should call repository") {

        val sourcesRepository: SourcesRepository = mockk(relaxed = true)

        val useCase = DeleteSourceUseCase(
            repository = sourcesRepository
        )

        useCase(folder = FakeUserFolders.folder1)

        coVerify { sourcesRepository.deleteFolder(FakeUserFolders.folder1) }

    }

})