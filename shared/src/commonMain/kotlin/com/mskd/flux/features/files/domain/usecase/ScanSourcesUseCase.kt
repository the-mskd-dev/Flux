package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.features.files.domain.model.SourcesScan

interface ScanSourcesUseCase {
    suspend operator fun invoke(): SourcesScan
}
