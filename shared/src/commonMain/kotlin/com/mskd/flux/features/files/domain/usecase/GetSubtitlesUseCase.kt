package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.domain.model.files.UserFile
import java.io.File

interface GetSubtitlesUseCase {
    suspend operator fun invoke(file: UserFile) : File?
}