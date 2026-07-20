package com.mskd.flux.features.sources.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mskd.flux.core.model.files.FileSource

@Entity(
    tableName = "folders"
)
data class UserFolderEntity(
    @PrimaryKey val path: String,
    val source: FileSource
)