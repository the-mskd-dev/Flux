package com.mskd.flux.features.sources.data.mapper

import com.mskd.flux.features.sources.data.local.UserFolderEntity
import com.mskd.flux.features.sources.domain.model.UserFolder

fun UserFolderEntity.toDomain(status: UserFolder.Status = UserFolder.Status.AVAILABLE) : UserFolder {
    return UserFolder(
        path = this.path,
        source = this.source,
        status = UserFolder.Status.AVAILABLE
    )
}

fun UserFolder.toEntity() : UserFolderEntity {
    return UserFolderEntity(
        path = this.path,
        source = this.source,
    )
}