package com.mskd.flux.features.sources.data.model

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