package com.mskd.flux.features.player.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.utils.extensions.getNextEpisodeFor

class GetNextEpisodeUseCase(
    private val database: DatabaseRepository,
) {

    suspend operator fun invoke(media: Media?) : Episode? {

        if (media == null || media !is Episode || media.isUnknown) return null

        val episodes = database.getEpisodes(artworkId = media.mediaId)

        return episodes.getNextEpisodeFor(episode = media)
    }
}