package com.mskd.flux.features.sources.usecase

import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.usecase.DeleteUnavailableSourcesUseCase
import com.mskd.flux.features.sources.domain.validator.UserFolderValidator
import com.mskd.flux.features.sources.fake.FakeSourcesRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.coEvery
import io.mockk.mockk

class DeleteUnavailableSourcesUseCaseTest : FunSpec({

   val validator = mockk<UserFolderValidator>()

    test("should only delete folders that are not LOCAL and MISSING") {

        val local = UserFolder(path = "path/local", source = FileSource.LOCAL)
        val safAvailable = UserFolder(path = "path/saf/available", source = FileSource.SAF)
        val safMissing = UserFolder(path = "path/saf/missing", source = FileSource.SAF)

        val repository = FakeSourcesRepository(
            initFolders = listOf(local, safAvailable, safMissing)
        )

        coEvery { validator.isFolderAvailable(safAvailable.path) } returns true
        coEvery { validator.isFolderAvailable(safMissing.path) } returns false

        val useCase = DeleteUnavailableSourcesUseCase(
            sources = repository,
            checkFolderDataSource = validator
        )

        useCase()

        val expectedRemainingFolders = listOf(local, safAvailable)

        repository.getFolders() shouldContainExactlyInAnyOrder expectedRemainingFolders

    }

})