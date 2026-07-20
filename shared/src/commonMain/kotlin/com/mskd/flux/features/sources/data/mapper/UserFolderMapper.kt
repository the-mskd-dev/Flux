package com.mskd.flux.features.sources.data.mapper

import com.mskd.flux.features.sources.data.local.UserFolderEntity
import com.mskd.flux.features.sources.domain.model.UserFolder

fun UserFolderEntity.toDomain(isAvailable: Boolean = true) : UserFolder {
    return UserFolder(
        path = this.path,
        source = this.source,
        isAvailable = isAvailable
    )
}

fun UserFolder.toEntity() : UserFolderEntity {
    return UserFolderEntity(
        path = this.path,
        source = this.source,
    )
}