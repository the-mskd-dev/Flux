package com.mskd.flux.features.token.domain.usecase

import com.mskd.flux.features.token.domain.model.AuthenticateResult

interface SaveTokenAndSyncUseCase {
    suspend operator fun invoke(token: String) : AuthenticateResult
}