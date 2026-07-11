package com.mskd.flux.features.token.data.usecase

import com.mskd.flux.core.network.tmdb.data.service.TMDBService
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.features.token.domain.model.AuthenticateResult
import com.mskd.flux.features.token.domain.usecase.SaveTokenAndSyncUseCase

class SaveTokenAndSyncUseCaseImpl(
    private val tokenDataStore: TokenDataStore,
    private val tmdbService: TMDBService,
    private val syncCatalogUseCase: SyncCatalogUseCase,
) : SaveTokenAndSyncUseCase {

    override suspend fun invoke(token: String): AuthenticateResult {

        return try {

            tokenDataStore.saveToken(token)

            val authentication = tmdbService.authenticate()

            if (authentication.success) {

                syncCatalogUseCase(onlyNew = false)
                AuthenticateResult.SUCCESS

            } else {

                tokenDataStore.clearToken()
                AuthenticateResult.FAILURE

            }

        } catch (e: Exception) {

            e.printStackTrace()
            tokenDataStore.clearToken()
            AuthenticateResult.FAILURE

        }

    }

}