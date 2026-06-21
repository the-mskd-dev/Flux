package com.mskd.flux.useCases.progress

import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.model.Status
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.getPreviousEpisodesFor
import com.mskd.flux.utils.extensions.lastEpisode
import com.mskd.flux.utils.extensions.timeDescription
import kotlin.time.Duration.Companion.minutes

interface ProgressUC {

    suspend fun saveProgress(media: Media, progress: Long)
    suspend fun changeMediaStatus(media: Media, status: Status)
    suspend fun markPreviousEpisodesAsWatchedFor(episode: Episode)
    suspend fun resetProgress(artwork: Artwork, season: Int?)
}