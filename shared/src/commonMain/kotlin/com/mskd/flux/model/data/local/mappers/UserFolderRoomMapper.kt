package com.mskd.flux.model.data.local.mappers

import com.mskd.flux.model.data.local.entities.UserFolderEntity
import com.mskd.flux.model.domain.files.UserFolder

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