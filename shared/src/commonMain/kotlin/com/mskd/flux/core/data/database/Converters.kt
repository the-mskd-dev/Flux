package com.mskd.flux.core.data.database

import androidx.room.TypeConverter
import com.mskd.flux.core.domain.model.artwork.ContentType
import com.mskd.flux.core.domain.model.files.FileSource

class Converters {

    @TypeConverter
    fun fromContentType(contentType: ContentType): String {
        return contentType.name
    }

    @TypeConverter
    fun toContentType(value: String): ContentType {
        return ContentType.valueOf(value)
    }

    @TypeConverter
    fun fromFileSource(fileSource: FileSource): String {
        return fileSource.name
    }

    @TypeConverter
    fun toFileSource(value: String): FileSource {
        return FileSource.valueOf(value)
    }
}