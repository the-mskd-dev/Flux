package com.mskd.flux.features.sources.repository

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.sources.data.local.SourcesDao
import com.mskd.flux.features.sources.data.local.UserFolderEntity
import com.mskd.flux.features.sources.data.repository.SourcesRepositoryImpl
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.validator.UserFolderValidator
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class SourcesRepositoryImplTest : FunSpec({

    fluxExtensions()

    // 1. Déclaration des mocks
    val dao = mockk<SourcesDao>(relaxed = true)
    val validator = mockk<UserFolderValidator>()

    val repository = SourcesRepositoryImpl(
        dao = dao,
        userFolderValidator = validator
    )

    test("flowFolders should map entities to domain and fetch status only for LOCAL sources") {
        val localEntity = UserFolderEntity(path = "path/local", source = FileSource.LOCAL)
        val safEntity = UserFolderEntity(path = "path/saf", source = FileSource.SAF)
        val safMissingEntity = UserFolderEntity(path = "path/safMissing", source = FileSource.SAF)

        every { dao.flowFolders() } returns flowOf(listOf(localEntity, safEntity, safMissingEntity))

        coEvery { validator.isFolderAvailable(safEntity.path) } returns UserFolder.Status.AVAILABLE
        coEvery { validator.isFolderAvailable(safMissingEntity.path) } returns UserFolder.Status.MISSING

        repository.flowFolders().test {
            val result = awaitItem()

            result shouldHaveSize 3

            val localResult = result.first { it.path == localEntity.path }
            localResult.status shouldBe UserFolder.Status.AVAILABLE

            val safResult = result.first { it.path == safEntity.path }
            safResult.status shouldBe UserFolder.Status.AVAILABLE

            val safMissingResult = result.first { it.path == safMissingEntity.path }
            safMissingResult.status shouldBe UserFolder.Status.MISSING

            awaitComplete()
        }

        coVerify(exactly = 0) { validator.isFolderAvailable(localEntity.path) }
    }

    test("saveFolder should convert domain to entity and call dao") {

        // Given
        val domainFolder = UserFolder(
            path = "new/path",
            source = FileSource.LOCAL,
            status = UserFolder.Status.AVAILABLE
        )

        // When
        repository.saveFolder(domainFolder)

        // Then
        coVerify(exactly = 1) {
            dao.insertFolder(folder = withArg {
                it.path shouldBe "new/path"
                it.source shouldBe FileSource.LOCAL
            })
        }
    }

})