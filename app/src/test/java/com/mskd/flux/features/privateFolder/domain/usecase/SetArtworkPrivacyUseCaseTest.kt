package com.mskd.flux.features.privateFolder.domain.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.features.privateFolder.domain.usecase.setArtworkPrivacy.SetArtworkPrivacyUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean
import io.mockk.coVerify
import io.mockk.mockk

class SetArtworkPrivacyUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var database: DatabaseRepository
    lateinit var history: HistoryRepository
    lateinit var useCase: SetArtworkPrivacyUseCase

    beforeTest {
        database = mockk(relaxed = true)
        history = mockk(relaxed = true)
        SetArtworkPrivacyUseCaseTest()
        useCase = SetArtworkPrivacyUseCase(
            database = database,
            history = history,
        )
    }

    test("apply private and delete in history if needed") {

        checkAll(
            iterations = 10,
            Exhaustive.boolean(),
            Arb.long()
        ) { isPrivate, artworkId ->

            // When
            useCase(artworkId = artworkId, isPrivate = isPrivate)

            // Then
            coVerify { database.setArtworkPrivate(artworkId = artworkId, isPrivate = isPrivate) }
            coVerify(exactly = if (isPrivate) 1 else 0) { history.delete(artworkId = artworkId) }

        }

    }

})