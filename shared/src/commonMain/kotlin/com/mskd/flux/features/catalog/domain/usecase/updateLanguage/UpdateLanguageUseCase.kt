package com.mskd.flux.features.catalog.domain.usecase.updateLanguage

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.SyncGenresUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class UpdateLanguageUseCase(
    private val api: ApiRepository,
    private val database: DatabaseRepository,
    private val settings: SettingsDataStore,
    private val coordinator: CatalogSyncCoordinator,
    private val syncGenresUseCase: SyncGenresUseCase,
    private val imagesPrefetchManager: ImagesPrefetchManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(10)
) {

    private companion object { const val TAG = "UpdateLanguageUseCase" }

    operator fun invoke() {
        coordinator.launch(full = false) {

            val language = settings.getDataLanguage()
            val artworks = database.getArtworks(includePrivates = true)
            val shows = artworks.filter { it.type == ContentType.SHOW }
            val medias = database.getMedias()
            val movies = medias.filterIsInstance<Movie>()
            val seasons = database.getSeasons()
            val episodes = medias.filterIsInstance<Episode>()

            val batchSize = 25

            supervisorScope {

                // Movies
                launch(dispatcher) {
                    movies.chunked(batchSize).forEach { chunk ->
                        val translated = chunk.map { movie ->
                            async {
                                api.getTranslation(
                                    request = TranslationRequest.Movie(artworkId = movie.artworkId, language = language)
                                )?.let { translation ->
                                    movie.copy(
                                        title = translation.title.orEmpty().ifBlank { movie.title },
                                        description = translation.description.orEmpty().ifBlank { movie.description }
                                    )
                                }
                            }
                        }.awaitAll().filterNotNull()

                        if (translated.isNotEmpty()) database.saveMedias(translated)
                    }
                }

                // Shows
                launch(dispatcher) {
                    shows.chunked(batchSize).forEach { chunk ->
                        val translated = chunk.map { show ->
                            async {
                                api.getTranslation(
                                    request = TranslationRequest.Show(artworkId = show.id, language = language)
                                )?.let { translation ->
                                    show.copy(
                                        title = translation.title.orEmpty().ifBlank { show.title },
                                        description = translation.description.orEmpty().ifBlank { show.description }
                                    )
                                }
                            }
                        }.awaitAll().filterNotNull()

                        if (translated.isNotEmpty()) database.saveArtworks(artworks = translated, overrideLastModification = false)
                    }
                }

                // Seasons
                launch(dispatcher) {
                    seasons.chunked(batchSize).forEach { chunk ->
                        val translated = chunk.map { season ->
                            async {
                                api.getTranslation(
                                    request = TranslationRequest.Season(artworkId = season.artworkId, season = season.season, language = language)
                                )?.let { translation ->
                                    season.copy(
                                        title = translation.title.orEmpty().ifBlank { season.title },
                                        description = translation.description.orEmpty().ifBlank { season.description }
                                    )
                                }
                            }
                        }.awaitAll().filterNotNull()

                        if (translated.isNotEmpty()) database.saveSeasons(translated)
                    }
                }

                // Episodes
                launch(dispatcher) {
                    episodes.chunked(batchSize).forEach { chunk ->
                        val translated = chunk.map { episode ->
                            async {
                                api.getTranslation(
                                    request = TranslationRequest.Episode(artworkId = episode.artworkId, season = episode.season, number = episode.number, language = language)
                                )?.let { translation ->
                                    episode.copy(
                                        title = translation.title.orEmpty().ifBlank { episode.title },
                                        description = translation.description.orEmpty().ifBlank { episode.description }
                                    )
                                }
                            }
                        }.awaitAll().filterNotNull()

                        if (translated.isNotEmpty()) database.saveMedias(translated)
                    }
                }

                // Genres
                launch(dispatcher) {
                    syncGenresUseCase()
                }

            }

            // Logos, once the translations are saved: the translated Artwork rows, read before the change, would overwrite the new Logo paths
            withContext(dispatcher) {
                artworks.filter { !it.isUnknown }.chunked(batchSize).forEach { chunk ->
                    chunk.map { artwork -> async { updateLogo(artwork) } }.awaitAll()
                }
            }

            imagesPrefetchManager.prefetchImages()

        }
    }

    /** A failed lookup keeps the previous Logo. */
    private suspend fun updateLogo(artwork: Artwork) {
        try {
            val logoPath = api.getLogo(artworkId = artwork.id, type = artwork.type)
            database.setArtworkLogo(artworkId = artwork.id, logoPath = logoPath)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Trace.error(TAG, "Fail to search the Logo of ${artwork.title}", e)
        }
    }

}
