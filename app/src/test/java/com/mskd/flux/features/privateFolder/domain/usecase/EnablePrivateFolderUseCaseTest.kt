package com.mskd.flux.features.privateFolder.domain.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.enablePrivateFolder.EnablePrivateFolderUseCase
import io.kotest.core.spec.style.FunSpec
import io.mockk.coVerify
import io.mockk.mockk

class EnablePrivateFolderUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var privateFolderDataStore: PrivateFolderDataStore
    lateinit var useCase: EnablePrivateFolderUseCase

    beforeTest {
        privateFolderDataStore = mockk(relaxed = true)
        useCase = EnablePrivateFolderUseCase(
            privateFolderDataStore = privateFolderDataStore,
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

})