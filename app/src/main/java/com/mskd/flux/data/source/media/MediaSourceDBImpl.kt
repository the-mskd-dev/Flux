package com.mskd.flux.data.source.media

import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.model.UserFile
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