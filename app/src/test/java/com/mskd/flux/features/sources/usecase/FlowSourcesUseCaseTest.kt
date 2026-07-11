package com.mskd.flux.features.sources.usecase

import app.cash.turbine.test
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import com.mskd.flux.features.sources.fake.FakeSourcesRepository
import com.mskd.flux.features.sources.fake.FakeUserFolders
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class FlowSourcesUseCaseTest : FunSpec( {

    test("use case should return flow from repository") {

        val folders = listOf(
            FakeUserFolders.folder1,
            FakeUserFolders.folder2,
            FakeUserFolders.folderMissing,
        )

        val repository = FakeSourcesRepository(initFolders = folders)

        val useCase = FlowSourcesUseCase(repository = repository)

        useCase().test {

            awaitItem() shouldContainExactlyInAnyOrder folders

        }

    }

})