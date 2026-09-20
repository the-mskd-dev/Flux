package com.mskd.flux.features.privateFolder.domain.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.disablePrivateFolder.DisablePrivateFolderUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class DisablePrivateFolderUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var privateFolderDataStore: PrivateFolderDataStore
    lateinit var database: DatabaseRepository
    lateinit var useCase: DisablePrivateFolderUseCase

    beforeTest {
        privateFolderDataStore = mockk(relaxed = true)
        database = mockk(relaxed = true)
        useCase = DisablePrivateFolderUseCase(
            privateFolderDataStore = privateFolderDataStore,
            database = database
        )
    }

    test("valid pin clears private artworks and disables the folder") {
        coEvery { privateFolderDataStore.verifyPin("1234") } returns true

        val result = useCase(pin = "1234")

        result shouldBe true
        coVerify(exactly = 1) { database.clearPrivateArtworks() }
        coVerify(exactly = 1) { privateFolderDataStore.setEnabled(enabled = false) }
    }

    test("wrong pin neither clears private artworks nor disables the folder") {
        coEvery { privateFolderDataStore.verifyPin("0000") } returns false

        val result = useCase(pin = "0000")

        result shouldBe false
        coVerify(exactly = 0) { database.clearPrivateArtworks() }
        coVerify(exactly = 0) { privateFolderDataStore.setEnabled(any()) }
    }

})
