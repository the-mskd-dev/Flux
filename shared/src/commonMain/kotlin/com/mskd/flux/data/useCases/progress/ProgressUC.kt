package com.mskd.flux.data.useCases.progress

import com.mskd.flux.model.domain.artwork.Status
import com.mskd.flux.model.domain.artwork.Artwork
import com.mskd.flux.model.domain.artwork.Episode
import com.mskd.flux.model.domain.artwork.Media

interface ProgressUC {

    suspend fun saveProgress(media: Media, progress: Long)
    suspend fun changeMediaStatus(media: Media, status: Status)
    suspend fun markPreviousEpisodesAsWatchedFor(episode: Episode)
    suspend fun resetProgress(artwork: Artwork, season: Int?)
}