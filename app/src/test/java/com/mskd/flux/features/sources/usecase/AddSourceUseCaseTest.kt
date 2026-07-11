package com.mskd.flux.features.sources.usecase

import com.mskd.flux.features.sources.domain.usecase.AddSourceUseCase
import com.mskd.flux.features.sources.fake.FakeSourcesRepository
import com.mskd.flux.features.sources.fake.FakeUserFolders
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class AddSourceUseCaseTest : FunSpec({

    test("add different folders") {

        val folders = listOf(
            FakeUserFolders.folder1,
            FakeUserFolders.folder2,
            FakeUserFolders.folderMissing,
        )

        val repository = FakeSourcesRepository()

        val useCase = AddSourceUseCase(
            repository = repository
        )

        folders.forEach {
            useCase(it) shouldBe true
        }

        repository.getFolders() shouldContainExactlyInAnyOrder folders

    }

    test("folder that already exists cannot be added") {

        val useCase = AddSourceUseCase(
            repository = FakeSourcesRepository(listOf(FakeUserFolders.folder1))
        )

        useCase(FakeUserFolders.folder1) shouldBe false

    }

    test("if a parent folder exists, a child folder cannot be added") {

        val useCase = AddSourceUseCase(
            repository = FakeSourcesRepository(listOf(FakeUserFolders.folder1))
        )

        useCase(FakeUserFolders.folder1sub1) shouldBe false

    }

    test("add parent folder removes child folders") {

        val repository = FakeSourcesRepository(listOf(FakeUserFolders.folder1sub1, FakeUserFolders.folder1sub2))

        val useCase = AddSourceUseCase(
            repository = repository
        )

        useCase(FakeUserFolders.folder1) shouldBe true

        repository.getFolders() shouldContainExactlyInAnyOrder  listOf(FakeUserFolders.folder1)

    }

})