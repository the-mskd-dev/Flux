package com.mskd.flux.features.privateFolder.domain.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.features.privateFolder.domain.usecase.markNsfwArtworksPrivate.MarkNsfwArtworksPrivateUseCase
import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class MarkNsfwArtworksPrivateUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var database: DatabaseRepository
    lateinit var history: HistoryRepository
    lateinit var useCase: MarkNsfwArtworksPrivateUseCase

    beforeTest {
        database = mockk(relaxed = true)
        history = mockk(relaxed = true)
        useCase = MarkNsfwArtworksPrivateUseCase(
            database = database,
            history = history
        )
    }

    test("mark every nsfw artwork as private and clear its history") {

        // Given
        val nsfwArtworkIds = listOf(1L, 2L, 3L)
        coEvery { database.getNsfwArtworkIds() } returns nsfwArtworkIds

        // When
        useCase()

        // Then
        coVerify(exactly = 1) { database.setNsfwArtworksPrivate() }
        nsfwArtworkIds.forEach { artworkId ->
            coVerify(exactly = 1) { history.delete(artworkId = artworkId) }
        }

    }

})
