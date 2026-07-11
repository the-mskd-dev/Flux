package com.mskd.flux.features.token.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.network.tmdb.data.dto.AuthenticationDto
import com.mskd.flux.core.network.tmdb.data.service.TMDBService
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.token.data.usecase.SaveTokenAndSyncUseCaseImpl
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.features.token.domain.model.AuthenticateResult
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SaveTokenAndSyncUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var tokenDataStore: TokenDataStore
    lateinit var tmdbService: TMDBService
    lateinit var syncCatalogUseCase: SyncCatalogUseCase
    lateinit var useCase: SaveTokenAndSyncUseCaseImpl

    beforeTest {
        tokenDataStore = mockk(relaxed = true)
        tmdbService = mockk(relaxed = true)
        syncCatalogUseCase = mockk(relaxed = true)
        useCase = SaveTokenAndSyncUseCaseImpl(
            tokenDataStore = tokenDataStore,
            tmdbService = tmdbService,
            syncCatalogUseCase = syncCatalogUseCase
        )
    }

    test("authenticate success saves token and syncs catalog") {
        val testToken = "test_token_123"
        coEvery { tmdbService.authenticate() } returns AuthenticationDto(success = true, code = 1, message = "Success")

        val result = useCase(testToken)

        result shouldBe AuthenticateResult.SUCCESS
        coVerify(exactly = 1) { tokenDataStore.saveToken(testToken) }
        coVerify(exactly = 1) { tmdbService.authenticate() }
        coVerify(exactly = 1) { syncCatalogUseCase(onlyNew = false) }
        coVerify(exactly = 0) { tokenDataStore.clearToken() }
    }

    test("authenticate failure clears token and returns FAILURE") {
        val testToken = "invalid_token"
        coEvery { tmdbService.authenticate() } returns AuthenticationDto(success = false, code = 30, message = "Invalid API Key")

        val result = useCase(testToken)

        result shouldBe AuthenticateResult.FAILURE
        coVerify(exactly = 1) { tokenDataStore.saveToken(testToken) }
        coVerify(exactly = 1) { tmdbService.authenticate() }
        coVerify(exactly = 1) { tokenDataStore.clearToken() }
        coVerify(exactly = 0) { syncCatalogUseCase(any()) }
    }

    test("exception during authenticate clears token and returns FAILURE") {
        val testToken = "error_token"
        coEvery { tmdbService.authenticate() } throws RuntimeException("Network error")

        val result = useCase(testToken)

        result shouldBe AuthenticateResult.FAILURE
        coVerify(exactly = 1) { tokenDataStore.saveToken(testToken) }
        coVerify(exactly = 1) { tokenDataStore.clearToken() }
        coVerify(exactly = 0) { syncCatalogUseCase(any()) }
    }

})
