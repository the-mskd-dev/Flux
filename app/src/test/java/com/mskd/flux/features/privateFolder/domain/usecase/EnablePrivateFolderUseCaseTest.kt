package com.mskd.flux.features.privateFolder.domain.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.enablePrivateFolder.EnablePrivateFolderUseCase
import com.mskd.flux.features.privateFolder.domain.usecase.markNsfwArtworksPrivate.MarkNsfwArtworksPrivateUseCase
import io.kotest.core.spec.style.FunSpec
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class EnablePrivateFolderUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var privateFolderDataStore: PrivateFolderDataStore
    lateinit var markNsfwArtworksPrivate: MarkNsfwArtworksPrivateUseCase
    lateinit var useCase: EnablePrivateFolderUseCase

    val privateFolderFlow = MutableStateFlow(PrivateFolderDataStore.State())

    beforeTest {
        privateFolderDataStore = mockk(relaxed = true) {
            every { flow } returns privateFolderFlow
        }
        markNsfwArtworksPrivate = mockk(relaxed = true)
        useCase = EnablePrivateFolderUseCase(
            privateFolderDataStore = privateFolderDataStore,
            markNsfwArtworksPrivate = markNsfwArtworksPrivate
        )
    }

    test("apply values in data store") {

        // Given
        val pin = "0000"

        // When
        useCase(pin = pin)

        // Then
        coVerify { privateFolderDataStore.setEnabled(true) }
        coVerify { privateFolderDataStore.setPin(pin) }

    }

    test("mark nsfw artworks as private when nsfw is included") {
        privateFolderFlow.value = PrivateFolderDataStore.State(includeNsfw = true)

        // When
        useCase(pin = "0000")

        // Then
        coVerify(exactly = 1) { markNsfwArtworksPrivate() }

    }

    test("do not mark nsfw artworks as private when nsfw is not included") {
        privateFolderFlow.value = PrivateFolderDataStore.State(includeNsfw = false)

        // When
        useCase(pin = "0000")

        // Then
        coVerify(exactly = 0) { markNsfwArtworksPrivate() }

    }

})
