package com.kaem.flux.data.source.media

import com.kaem.flux.data.ddb.DatabaseDao
import com.kaem.flux.model.UserFile
import javax.inject.Inject

class MediaSourceDBImpl @Inject constructor(
    private val db: DatabaseDao
) : MediaSource {

    override suspend fun getMedias(
        files: List<UserFile>
    ): MediaSource.Library {

        return MediaSource.Library(artworks = db.getArtworks())

    }

}