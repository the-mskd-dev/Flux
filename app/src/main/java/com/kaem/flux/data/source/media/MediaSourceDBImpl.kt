package com.kaem.flux.data.source.media

import com.kaem.flux.data.ddb.DatabaseDao
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.model.media.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaSourceDBImpl @Inject constructor(
    private val db: DatabaseDao
) : MediaSource {

    override suspend fun getMedias(
        files: List<UserFile>
    ): MediaSource.Library {

        return MediaSource.Library(overviews = db.getOverviews())

    }

}