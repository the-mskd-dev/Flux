package com.mskd.flux.features.privateFolder.domain.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.setIncludeNsfw.SetIncludeNsfwUseCase
import com.mskd.flux.features.progress.domain.ProgressTracker
import io.kotest.core.spec.style.FunSpec
import io.mockk.coVerify
import io.mockk.mockk

class SetIncludeNsfwUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var privateFolderDataStore: PrivateFolderDataStore
    lateinit var progressTracker: ProgressTracker
    lateinit var useCase: SetIncludeNsfwUseCase

    beforeTest {
        privateFolderDataStore = mockk(relaxed = true)
        progressTracker = mockk(relaxed = true)
        useCase = SetIncludeNsfwUseCase(
            privateFolderDataStore = privateFolderDataStore,
            progressTracker = progressTracker
        )
    }

    test("apply value in data store") {

        // Given
        val includeNsfw = true

        // When
        useCase(includeNsfw = includeNsfw)

        // Then
        coVerify { privateFolderDataStore.setIncludeNsfw(includeNsfw = includeNsfw) }

    }

    test("mark nsfw artworks as private when included") {

        // When
        useCase(includeNsfw = true)

        // Then
        coVerify(exactly = 1) { progressTracker.markNsfwArtworksPrivate() }

    }

    test("do not mark nsfw artworks as private when not included") {

        // When
        useCase(includeNsfw = false)

        // Then
        coVerify(exactly = 0) { progressTracker.markNsfwArtworksPrivate() }

    }

})
