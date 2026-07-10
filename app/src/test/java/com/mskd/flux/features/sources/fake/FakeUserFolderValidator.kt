package com.mskd.flux.features.sources.fake

import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.validator.UserFolderValidator

class FakeUserFolderValidator(private val returnValue: UserFolder.Status = UserFolder.Status.AVAILABLE) : UserFolderValidator {

    override suspend fun isFolderAvailable(path: String): UserFolder.Status {
        return returnValue
    }

}