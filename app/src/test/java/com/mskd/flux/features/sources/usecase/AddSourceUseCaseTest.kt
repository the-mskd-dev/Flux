package com.mskd.flux.features.sources.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.features.sources.domain.usecase.AddSourceUseCase
import com.mskd.flux.features.sources.fake.FakeSourcesRepository
import com.mskd.flux.features.sources.fake.FakeUserFolders
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first

class AddSourceUseCaseTest : FunSpec({

    fluxExtensions()

    test("add different folders") {

        val folders = listOf(
            FakeUserFolders.folder1,
            FakeUserFolders.folder2,
            FakeUserFolders.folder3,
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


})