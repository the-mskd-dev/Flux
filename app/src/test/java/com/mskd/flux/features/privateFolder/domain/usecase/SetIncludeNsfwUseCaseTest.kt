package com.mskd.flux.features.privateFolder.domain.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.markNsfwArtworksPrivate.MarkNsfwArtworksPrivateUseCase
import com.mskd.flux.features.privateFolder.domain.usecase.setIncludeNsfw.SetIncludeNsfwUseCase
import io.kotest.core.spec.style.FunSpec
import io.mockk.coVerify
import io.mockk.mockk

class SetIncludeNsfwUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var privateFolderDataStore: PrivateFolderDataStore
    lateinit var markNsfwArtworksPrivate: MarkNsfwArtworksPrivateUseCase
    lateinit var useCase: SetIncludeNsfwUseCase

    beforeTest {
        privateFolderDataStore = mockk(relaxed = true)
        markNsfwArtworksPrivate = mockk(relaxed = true)
        useCase = SetIncludeNsfwUseCase(
            privateFolderDataStore = privateFolderDataStore,
            markNsfwArtworksPrivate = markNsfwArtworksPrivate
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
        coVerify(exactly = 1) { markNsfwArtworksPrivate() }

    }

    test("do not mark nsfw artworks as private when not included") {

        // When
        useCase(includeNsfw = false)

        // Then
        coVerify(exactly = 0) { markNsfwArtworksPrivate() }

    }

})
