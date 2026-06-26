package com.mskd.flux.useCases.player

interface PipIsEnabledUC {
    suspend operator fun invoke(): Boolean
}