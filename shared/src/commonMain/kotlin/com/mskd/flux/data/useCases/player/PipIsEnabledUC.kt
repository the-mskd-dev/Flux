package com.mskd.flux.data.useCases.player

interface PipIsEnabledUC {
    suspend operator fun invoke(): Boolean
}