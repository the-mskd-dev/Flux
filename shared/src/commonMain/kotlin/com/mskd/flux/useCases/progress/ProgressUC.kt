package com.mskd.flux.useCases.progress

import com.mskd.flux.model.Status
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media

interface ProgressUC {

    suspend fun saveProgress(media: Media, progress: Long)
    suspend fun changeMediaStatus(media: Media, status: Status)
    suspend fun markPreviousEpisodesAsWatchedFor(episode: Episode)
    suspend fun resetProgress(artwork: Artwork, season: Int?)
}