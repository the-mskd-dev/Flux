package com.mskd.flux.model.data.local.entities

import androidx.room.Entity
import com.mskd.flux.model.domain.FileSource

@Entity(
    tableName = "folders"
)
data class UserFolderEntity(
    val path: String,
    val source: FileSource
)
