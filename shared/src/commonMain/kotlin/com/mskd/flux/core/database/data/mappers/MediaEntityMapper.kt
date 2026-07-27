package com.mskd.flux.core.database.data.mappers

import com.mskd.flux.core.database.data.model.MediaEntity
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.files.UserFile

fun MediaEntity.toDomain() : Media {
    return when (this.type) {
        ContentType.SHOW -> {
            Episode(
                id = this.id,
                artworkId = this.artworkId,
                title = this.title,
                season = this.season!!,
                number = this.number!!,
                imagePath = this.imagePath!!,
                releaseDateString = this.releaseDateString,
                description = this.description,
                voteAverage = this.voteAverage,
                voteCount = this.voteCount,
                duration = this.duration,
                currentTime = this.currentTime,
                status = this.status,
                file = UserFile(
                    name = this.fileName,
                    addedDateTime = this.addedDateTime,
                    path = this.path,
                    realPath = this.realPath,
                    source = this.source,
                    parentDocId = this.parentDocId
                ),
                isAvailable = true,
            )
        }
        ContentType.MOVIE -> {
            Movie(
                artworkId = this.artworkId,
                title = this.title,
                releaseDateString = this.releaseDateString,
                description = this.description,
                voteAverage = this.voteAverage,
                voteCount = this.voteCount,
                duration = this.duration,
                currentTime = this.currentTime,
                status = this.status,
                file = UserFile(
                    name = this.fileName,
                    addedDateTime = this.addedDateTime,
                    path = this.path,
                    realPath = this.realPath,
                    source = this.source,
                    parentDocId = this.parentDocId
                ),
                isAvailable = true,
            )
        }
    }
}

fun Media.toEntity() : MediaEntity {
    return when (this) {
        is Episode -> {
            MediaEntity(
                id = this.id,
                artworkId = this.artworkId,
                type = ContentType.SHOW,
                number = this.number,
                season = this.season,
                imagePath = this.imagePath,
                title = this.title,
                releaseDateString = this.releaseDateString,
                description = this.description,
                voteAverage = this.voteAverage,
                voteCount = this.voteCount,
                duration = this.duration,
                currentTime = this.currentTime,
                status = this.status,
                fileName = this.file.name,
                addedDateTime = this.file.addedDateTime,
                path = this.file.path,
                realPath = this.file.realPath,
                source = this.file.source,
                parentDocId = this.file.parentDocId
            )
        }
        is Movie -> {
            MediaEntity(
                id = this.artworkId,
                artworkId = this.artworkId,
                type = ContentType.MOVIE,
                number = null,
                season = null,
                imagePath = null,
                title = this.title,
                releaseDateString = this.releaseDateString,
                description = this.description,
                voteAverage = this.voteAverage,
                voteCount = this.voteCount,
                duration = this.duration,
                currentTime = this.currentTime,
                status = this.status,
                fileName = this.file.name,
                addedDateTime = this.file.addedDateTime,
                path = this.file.path,
                realPath = this.file.realPath,
                source = this.file.source,
                parentDocId = this.file.parentDocId
            )
        }
    }
}