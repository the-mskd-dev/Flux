package com.mskd.flux.features.sources.repository

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
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
    lateinit var dao: SourcesDao
    lateinit var databaseRepository: DatabaseRepository
    lateinit var validator: UserFolderValidator
    lateinit var repository: SourcesRepositoryImpl

    beforeTest {
        dao = mockk(relaxed = true)
        databaseRepository = mockk(relaxed = true)
        validator = mockk()
        repository = SourcesRepositoryImpl(
            dao = dao,
            databaseRepository = databaseRepository,
            userFolderValidator = validator
        )
    }

    test("flowFolders should map entities to domain and fetch status only for LOCAL sources") {
        val localEntity = UserFolderEntity(path = "path/local", source = FileSource.LOCAL)
        val safEntity = UserFolderEntity(path = "path/saf", source = FileSource.SAF)
        val safMissingEntity = UserFolderEntity(path = "path/safMissing", source = FileSource.SAF)

        every { dao.flowFolders() } returns flowOf(listOf(localEntity, safEntity, safMissingEntity))

        coEvery { validator.isFolderAvailable(safEntity.path) } returns true
        coEvery { validator.isFolderAvailable(safMissingEntity.path) } returns false

        repository.flowFolders().test {
            val result = awaitItem()

            result shouldHaveSize 3

            val localResult = result.first { it.path == localEntity.path }
            localResult.isAvailable shouldBe true

            val safResult = result.first { it.path == safEntity.path }
            safResult.isAvailable shouldBe true

            val safMissingResult = result.first { it.path == safMissingEntity.path }
            safMissingResult.isAvailable shouldBe false

            awaitComplete()
        }

        coVerify(exactly = 0) { validator.isFolderAvailable(localEntity.path) }
    }

    test("saveFolder should convert domain to entity and call dao") {

        // Given
        val domainFolder = UserFolder(
            path = "new/path",
            source = FileSource.LOCAL,
            isAvailable = true
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

    test("deleteFolder with deleteMedias true should delete folder from dao and delete medias from database") {
        val folder = UserFolder(path = "path/to/delete", source = FileSource.LOCAL, isAvailable = true)

        repository.deleteFolder(folder, deleteMedias = true)

        coVerify(exactly = 1) { dao.deleteFolder(path = folder.path) }
        coVerify(exactly = 1) { databaseRepository.deleteMediasInFolder(folder = folder) }
    }

    test("deleteFolder with deleteMedias false should delete folder from dao but NOT delete medias from database") {
        val folder = UserFolder(path = "path/to/delete", source = FileSource.LOCAL, isAvailable = true)

        repository.deleteFolder(folder, deleteMedias = false)

        coVerify(exactly = 1) { dao.deleteFolder(path = folder.path) }
        coVerify(exactly = 0) { databaseRepository.deleteMediasInFolder(any()) }
    }

    test("deleteFolders with deleteMedias true should delete folders from dao and delete medias from database for all folders") {
        val folder1 = UserFolder(path = "path/1", source = FileSource.LOCAL, isAvailable = true)
        val folder2 = UserFolder(path = "path/2", source = FileSource.LOCAL, isAvailable = true)

        repository.deleteFolders(listOf(folder1, folder2), deleteMedias = true)

        coVerify(exactly = 1) { dao.deleteFolders(paths = listOf("path/1", "path/2")) }
        coVerify(exactly = 1) { databaseRepository.deleteMediasInFolder(folder = folder1) }
        coVerify(exactly = 1) { databaseRepository.deleteMediasInFolder(folder = folder2) }
    }

    test("deleteFolders with deleteMedias false should delete folders from dao but NOT delete medias from database") {
        val folder1 = UserFolder(path = "path/1", source = FileSource.LOCAL, isAvailable = true)
        val folder2 = UserFolder(path = "path/2", source = FileSource.LOCAL, isAvailable = true)

        repository.deleteFolders(listOf(folder1, folder2), deleteMedias = false)

        coVerify(exactly = 1) { dao.deleteFolders(paths = listOf("path/1", "path/2")) }
        coVerify(exactly = 0) { databaseRepository.deleteMediasInFolder(any()) }
    }

})