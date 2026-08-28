package com.mskd.flux.features.progress.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.timeDescription
import kotlin.time.Duration.Companion.minutes

class SaveProgressUseCase(private val database: DatabaseRepository) {

    companion object {
        const val TAG = "SaveProgressUseCase"
    }

    suspend operator fun invoke(
        media: Media,
        progress: Long
    ) : Media {

        val newStatus = if (progress >= (media.duration * Constants.PLAYER.PROGRESS_THRESHOLD).minutes.inWholeMilliseconds) Status.WATCHED else Status.IS_WATCHING
        val newTime = if (newStatus == Status.WATCHED) 0L else progress

        val updatedMedia = when (media) {
            is Movie -> media.copy(currentTime = newTime, status = newStatus)
            is Episode -> media.copy(currentTime = newTime, status = newStatus)
        }

        // Save in DB
        database.saveMedias(listOf(updatedMedia))

        Trace.info(TAG, "${updatedMedia.title} saved at ${progress.timeDescription()}")

        return updatedMedia

    }

}