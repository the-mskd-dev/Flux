package com.mskd.flux.features.catalog.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.catalog.domain.usecase.cleanCatalog.CleanCatalogUseCaseImpl
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class CleanCatalogUseCaseTest : FunSpec({

    fluxExtensions()

    val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
    val database = mockk<DatabaseRepository>(relaxed = true)
    val useCase = CleanCatalogUseCaseImpl(
        getDeviceFilesUseCase = getDeviceFilesUseCase,
        database = database
    )

    test("invoke deletes medias not in device files") {
        val dummyFiles = listOf<UserFile>(mockk(), mockk())
        coEvery { getDeviceFilesUseCase() } returns dummyFiles

        useCase()

        coVerify(exactly = 1) { getDeviceFilesUseCase() }
        coVerify(exactly = 1) { database.deleteMediasNotInFiles(dummyFiles) }
    }

})
