package com.mskd.flux.features.catalog.domain.sync.enrichment

import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException

/** Title and description in the user language for the Médias that miss them. A failure keeps the Média as it is. */
class TranslationsEnrichment(
    private val api: ApiRepository,
    private val settings: SettingsDataStore,
) : Enrichment {

    private companion object { const val TAG = "TranslationsEnrichment" }

    override fun units(folder: CatalogFolder) = folder.files.size

    override suspend fun enrich(draft: FolderDraft) {

        val language = settings.getDataLanguage()

        draft.medias = draft.medias.map { media ->

            if (draft.isUnknown || (media.title.isNotBlank() && media.description.isNotBlank())) return@map media

            try {

                val request = when (media) {
                    is Episode -> TranslationRequest.Episode(
                        artworkId = media.artworkId,
                        season = media.season,
                        number = media.number,
                        language = language
                    )
                    is Movie -> TranslationRequest.Movie(artworkId = media.artworkId, language = language)
                }

                val translation = api.getTranslation(request = request) ?: return@map media
                media.withText(
                    title = translation.title ?: media.title,
                    description = translation.description ?: media.description
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Trace.error(TAG, "Fail to translate ${media.file.name}", e)
                media
            }

        }

    }

    private fun Media.withText(title: String, description: String): Media = when (this) {
        is Episode -> copy(title = title, description = description)
        is Movie -> copy(title = title, description = description)
    }

}
