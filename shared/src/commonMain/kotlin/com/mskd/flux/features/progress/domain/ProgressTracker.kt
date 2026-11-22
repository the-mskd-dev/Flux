package com.mskd.flux.features.progress.domain

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.domain.model.VideoChapterType
import com.mskd.flux.features.player.domain.usecase.GetChaptersUseCase
import com.mskd.flux.features.sources.domain.extension.isAvailableFor
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.report.Trace
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.timeDescription
import kotlin.time.Duration.Companion.minutes

/**
 * Owns the domain rules for Media Progress, Status, History entries, Private exclusion,
 * and Next Episode lookup.
 */
class ProgressTracker(
    private val database: DatabaseRepository,
    private val history: HistoryRepository,
    private val getChapters: GetChaptersUseCase,
    private val sources: SourcesRepository,
) {

    private companion object {
        const val TAG = "ProgressTracker"
    }

    /**
     * Records playback [position] on [media], resolving its new Status from Chapters or the
     * progress threshold unless [forceWatched] is true, saves the Media, and applies the History rule.
     */
    suspend fun recordProgress(media: Media, position: Long, forceWatched: Boolean = false): Media {
        val newStatus = if (forceWatched) {
            Status.WATCHED
        } else {
            // Chapters are irrelevant when the watched status is forced
            resolveStatus(
                position = position,
                durationMinutes = media.duration,
                chapters = getChapters(media),
            )
        }
        val newTime = if (newStatus == Status.WATCHED) 0L else position
        val updatedMedia = media.withProgress(status = newStatus, currentTime = newTime)

        database.saveMedias(listOf(updatedMedia))
        Trace.info(TAG, "${updatedMedia.title} saved at ${position.timeDescription()}")
        updateHistory(media = updatedMedia)

        return updatedMedia
    }

    /**
     * Returns the Next Episode after [media], or null for null, Movies, the Unknown Artwork, or the
     * last Episode. When [onlyAvailable] is true, verifies real Source availability without skipping.
     */
    suspend fun nextEpisode(media: Media?, onlyAvailable: Boolean = false): Episode? {
        if (media !is Episode || media.isUnknown) return null

        val next = database.getNextEpisodeFor(episode = media) ?: return null
        if (!onlyAvailable) return next

        return next.takeIf { sources.getFolders().isAvailableFor(file = it.file) }
    }

    private suspend fun updateHistory(media: Media) {
        if (media is Episode && media.isUnknown) return
        if (database.getArtwork(artworkId = media.artworkId)?.isPrivate == true) return

        when (media.status) {
            Status.IS_WATCHING -> history.insert(media = media)
            Status.WATCHED -> when (media) {
                is Movie -> history.delete(artworkId = media.artworkId)
                is Episode -> {
                    // Keep an unavailable Next Episode in History so GetHistoryUseCase restores it when the Source returns
                    val next = nextEpisode(media = media, onlyAvailable = false)
                    if (next != null) history.insert(media = next) else history.delete(artworkId = media.artworkId)
                }
            }
            Status.TO_WATCH -> Unit
        }
    }

    private fun resolveStatus(position: Long, durationMinutes: Int, chapters: List<VideoChapter>): Status {
        val lastChapter = chapters.maxByOrNull { it.index }
        return when {
            lastChapter != null && position in lastChapter.start..lastChapter.end -> {
                if (lastChapter.type == VideoChapterType.ENDING) Status.WATCHED else Status.IS_WATCHING
            }
            position >= (durationMinutes * Constants.PLAYER.PROGRESS_THRESHOLD).minutes.inWholeMilliseconds -> Status.WATCHED
            else -> Status.IS_WATCHING
        }
    }

    private fun Media.withProgress(status: Status, currentTime: Long): Media = when (this) {
        is Movie -> copy(status = status, currentTime = currentTime)
        is Episode -> copy(status = status, currentTime = currentTime)
    }

}
