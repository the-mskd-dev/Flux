package com.mskd.flux.features.catalog.domain.resolver

import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.files.domain.usecase.GetFileDurationUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

interface MediaResolver {

    suspend fun resolve(medias: List<Media>, onProgress: () -> Unit) : List<Media>

}

class MediaResolverImpl(
    private val api: ApiRepository,
    private val settings: SettingsDataStore,
    private val getFileDurationUseCase: GetFileDurationUseCase,
    private val dispatcher: CoroutineDispatcher
) : MediaResolver {

    private companion object { const val TAG = "MediaResolver" }

    override suspend fun resolve(medias: List<Media>, onProgress: () -> Unit): List<Media> {

        val language = settings.getDataLanguage()

        val resolvedEpisodes = supervisorScope {

            medias.map { media ->

                async(dispatcher) {

                    // Get translation if needed
                    val translation = when (media) {
                        is Episode -> {
                            if (!media.isUnknown && (media.title.isBlank() || media.description.isBlank())) {
                                api.getTranslation(
                                    request = TranslationRequest.Episode(
                                        artworkId = media.artworkId,
                                        season = media.season,
                                        number = media.number,
                                        language = language
                                    )
                                )
                            } else null
                        }

                        is Movie -> {
                            if (media.title.isBlank() || media.description.isBlank()) {
                                api.getTranslation(
                                    request = TranslationRequest.Movie(
                                        artworkId = media.artworkId,
                                        language = language
                                    )
                                )
                            } else null
                        }
                    }

                    // Get new values
                    val newTitle = translation?.title ?: media.title
                    val newDescription = translation?.description ?: media.description
                    val newDuration = if (media.duration > 0) media.duration else getFileDurationUseCase(file = media.file)

                    // Create resolved media
                    val resolvedMedia = if (newTitle == media.title && newDescription == media.description && newDuration == media.duration) {
                        media
                    } else {
                        when (media) {
                            is Episode -> media.copy(
                                title = newTitle,
                                description = newDescription,
                                duration = newDuration
                            )
                            is Movie -> media.copy(
                                title = newTitle,
                                description = newDescription,
                                duration = newDuration
                            )
                        }
                    }

                    onProgress()

                    resolvedMedia

                }

            }.awaitAll()

        }

        return resolvedEpisodes

    }

}