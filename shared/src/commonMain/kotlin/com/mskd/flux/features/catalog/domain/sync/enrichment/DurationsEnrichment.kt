package com.mskd.flux.features.catalog.domain.sync.enrichment

import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.features.files.domain.usecase.GetFileDurationUseCase
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException

/** Duration read from the file when TMDB does not provide it. A failure keeps the Média as it is. */
class DurationsEnrichment(private val getFileDurationUseCase: GetFileDurationUseCase) : Enrichment {

    private companion object { const val TAG = "DurationsEnrichment" }

    override fun units(folder: CatalogFolder) = folder.files.size

    override suspend fun enrich(draft: FolderDraft) {

        draft.medias = draft.medias.map { media ->

            if (media.duration > 0) return@map media

            try {
                media.withDuration(getFileDurationUseCase(file = media.file))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Trace.error(TAG, "Fail to read the duration of ${media.file.name}", e)
                media
            }

        }

    }

    private fun Media.withDuration(duration: Int): Media = when (this) {
        is Episode -> copy(duration = duration)
        is Movie -> copy(duration = duration)
    }

}
