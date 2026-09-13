package com.mskd.flux.features.history.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.utils.extensions.getNextEpisodeFor

class SaveToHistoryUseCase(
    private val history: HistoryRepository,
    private val database: DatabaseRepository
) {

    suspend operator fun invoke(media: Media) {

        when (media) {
            is Episode -> {

                if (media.isUnknown)
                    return

                if (media.status == Status.IS_WATCHING) {
                    history.insert(media = media)
                } else {
                    val episodes = database.getEpisodes(artworkId = media.artworkId)
                    val nextEpisode = episodes.getNextEpisodeFor(episode = media)
                    if (nextEpisode != null) {
                        history.insert(media = nextEpisode)
                    } else {
                        history.delete(artworkId = media.artworkId)
                    }
                }

            }
            is Movie -> {

                if (media.status == Status.IS_WATCHING)
                    history.insert(media = media)
                else
                    history.delete(artworkId = media.artworkId)

            }
        }

    }

}