package com.mskd.flux.model.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mskd.flux.model.domain.files.FileSource

@Entity(
    tableName = "folders"
)
data class UserFolderEntity(
    @PrimaryKey val path: String,
    val source: FileSource
)
