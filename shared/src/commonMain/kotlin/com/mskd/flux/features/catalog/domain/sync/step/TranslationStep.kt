package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.network.tmdb.domain.model.Translation
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.catalog.domain.model.SyncType
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Translates the Catalog titles and descriptions (Movies, Shows, Seasons and Episodes, including Private Artwork and
 * Unavailable Media, excluding the Unknown Artwork) into the selected data language during a Language change.
 *
 * Runs before [LogosStep] so saved Artwork rows never overwrite newly updated Logo paths. Missing or blank translated
 * fields keep their previous values, and a failed lookup leaves the item untouched with no Failed file recorded.
 */
class TranslationStep(
    private val api: ApiRepository,
    private val database: DatabaseRepository,
    private val settings: SettingsDataStore,
    private val tokenDataStore: TokenDataStore,
) : SyncStep {

    private companion object {
        const val TAG = "TranslationStep"
        const val BATCH_SIZE = 25
        const val MAX_PARALLELISM = 10
    }

    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(MAX_PARALLELISM)

    override fun isEnabled(context: SyncContext): Boolean = context.type == SyncType.LANGUAGE_CHANGE

    override suspend fun units(context: SyncContext): Int = translatableCatalog().totalItems

    override suspend fun run(context: SyncContext) {

        val catalog = translatableCatalog()
        if (catalog.totalItems == 0) return

        val language = settings.getDataLanguage()

        coroutineScope {

            launch(dispatcher) {
                translateInBatches(
                    items = catalog.movies,
                    context = context,
                    request = { TranslationRequest.Movie(artworkId = it.artworkId, language = language) },
                    apply = { movie, translation ->
                        movie.copy(
                            title = translation.title.orKeep(movie.title),
                            description = translation.description.orKeep(movie.description),
                        )
                    },
                    save = { translatedMovies ->
                        database.saveSyncedMedias(translatedMovies)
                        val translatedArtworks = translatedMovies
                            .mapNotNull { movie ->
                                catalog.movieArtworksById[movie.artworkId]?.copy(
                                    title = movie.title,
                                    description = movie.description,
                                )
                            }
                            .distinctBy { it.id }
                        if (translatedArtworks.isNotEmpty()) {
                            database.updateArtworksTranslations(translatedArtworks)
                        }
                    },
                )
            }

            launch(dispatcher) {
                translateInBatches(
                    items = catalog.shows,
                    context = context,
                    request = { TranslationRequest.Show(artworkId = it.id, language = language) },
                    apply = { show, translation ->
                        show.copy(
                            title = translation.title.orKeep(show.title),
                            description = translation.description.orKeep(show.description),
                        )
                    },
                    save = { translatedShows ->
                        database.updateArtworksTranslations(translatedShows)
                    },
                )
            }

            launch(dispatcher) {
                translateInBatches(
                    items = catalog.seasons,
                    context = context,
                    request = { TranslationRequest.Season(artworkId = it.artworkId, season = it.season, language = language) },
                    apply = { season, translation ->
                        season.copy(
                            title = translation.title.orKeep(season.title),
                            description = translation.description.orKeep(season.description),
                        )
                    },
                    save = { translatedSeasons -> database.saveSeasons(translatedSeasons) },
                )
            }

            launch(dispatcher) {
                translateInBatches(
                    items = catalog.episodes,
                    context = context,
                    request = {
                        TranslationRequest.Episode(
                            artworkId = it.artworkId,
                            season = it.season,
                            number = it.number,
                            language = language,
                        )
                    },
                    apply = { episode, translation ->
                        episode.copy(
                            title = translation.title.orKeep(episode.title),
                            description = translation.description.orKeep(episode.description),
                        )
                    },
                    save = { translatedEpisodes -> database.saveSyncedMedias(translatedEpisodes) },
                )
            }

        }

    }

    /** None without a TMDB token: lookups could only fail. */
    private suspend fun translatableCatalog(): TranslatableCatalog {
        if (!tokenDataStore.tokenIsAvailable()) return TranslatableCatalog()

        val artworks = database.getArtworks(includePrivates = true).filterNot { it.isUnknown }
        val medias = database.getMedias()
        val seasons = database.getSeasons().filter { it.artworkId != Artwork.UNKNOWN_ID }

        return TranslatableCatalog(
            movies = medias.filterIsInstance<Movie>().filter { it.artworkId != Artwork.UNKNOWN_ID },
            movieArtworksById = artworks.filter { it.type == ContentType.MOVIE }.associateBy { it.id },
            shows = artworks.filter { it.type == ContentType.SHOW },
            seasons = seasons,
            episodes = medias.filterIsInstance<Episode>().filterNot { it.isUnknown },
        )
    }

    private suspend fun <T> translateInBatches(
        items: List<T>,
        context: SyncContext,
        request: (T) -> TranslationRequest,
        apply: (T, Translation) -> T,
        save: suspend (List<T>) -> Unit,
    ) {
        items.chunked(BATCH_SIZE).forEach { chunk ->
            val translated = coroutineScope {
                chunk.map { item ->
                    async {
                        translate(item, request, apply).also { context.progress.complete() }
                    }
                }.awaitAll()
            }.filterNotNull()

            if (translated.isNotEmpty()) save(translated)
        }
    }

    private suspend fun <T> translate(
        item: T,
        request: (T) -> TranslationRequest,
        apply: (T, Translation) -> T,
    ): T? = try {
        api.getTranslation(request = request(item))?.let { translation -> apply(item, translation) }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Trace.error(TAG, "Fail to translate ${request(item)}", e)
        null
    }

    private fun String?.orKeep(previous: String): String = orEmpty().ifBlank { previous }

    private data class TranslatableCatalog(
        val movies: List<Movie> = emptyList(),
        val movieArtworksById: Map<Long, Artwork> = emptyMap(),
        val shows: List<Artwork> = emptyList(),
        val seasons: List<Season> = emptyList(),
        val episodes: List<Episode> = emptyList(),
    ) {
        val totalItems: Int get() = movies.size + shows.size + seasons.size + episodes.size
    }

}
