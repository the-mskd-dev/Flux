package com.mskd.flux.features.sources.fake

import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.validator.UserFolderValidator

class FakeUserFolderValidator : UserFolderValidator {

    override suspend fun isFolderAvailable(path: String): UserFolder.Status {
        return UserFolder.Status.AVAILABLE
    }

}