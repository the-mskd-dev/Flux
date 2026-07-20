package com.mskd.flux.features.player.data

interface PipIsEnabledUseCase {
    suspend operator fun invoke(): Boolean
}