package com.mskd.flux.features.sources.data.model

import com.mskd.flux.features.sources.domain.model.UserFolder

fun UserFolderEntity.toDomain() : UserFolder {
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