package com.mskd.flux.features.catalog.domain.usecase.updateLanguage

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.core.network.tmdb.domain.repository.ArtworkRemoteRepository
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class UpdateLanguageUseCaseImpl(
    private val remoteRepository: ArtworkRemoteRepository,
    private val database: DatabaseRepository,
    private val settings: SettingsDataStore,
    private val coordinator: CatalogSyncCoordinator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(10)
) : UpdateLanguageUseCase {

    override fun invoke() {
        coordinator.launch(full = false) {

            val language = settings.getDataLanguage()
            val shows = database.getArtworks().filter { it.type == ContentType.SHOW }
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
                                remoteRepository.translate(
                                    request = TranslationRequest.Movie(artworkId = movie.artworkId, language = language)
                                )?.let { translation ->
                                    movie.copy(
                                        title = translation.title ?: movie.title,
                                        description = translation.description ?: movie.description
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
                                remoteRepository.translate(
                                    request = TranslationRequest.Show(artworkId = show.id, language = language)
                                )?.let { translation ->
                                    show.copy(
                                        title = translation.title ?: show.title,
                                        description = translation.description ?: show.description
                                    )
                                }
                            }
                        }.awaitAll().filterNotNull()

                        if (translated.isNotEmpty()) database.saveArtworks(translated)
                    }
                }

                // Seasons
                launch(dispatcher) {
                    seasons.chunked(batchSize).forEach { chunk ->
                        val translated = chunk.map { season ->
                            async {
                                remoteRepository.translate(
                                    request = TranslationRequest.Season(artworkId = season.artworkId, season = season.season, language = language)
                                )?.let { translation ->
                                    season.copy(
                                        title = translation.title ?: season.title,
                                        description = translation.description ?: season.description
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
                                remoteRepository.translate(
                                    request = TranslationRequest.Episode(artworkId = episode.artworkId, season = episode.season, number = episode.number, language = language)
                                )?.let { translation ->
                                    episode.copy(
                                        title = translation.title ?: episode.title,
                                        description = translation.description ?: episode.description
                                    )
                                }
                            }
                        }.awaitAll().filterNotNull()

                        if (translated.isNotEmpty()) database.saveMedias(translated)
                    }
                }


            }

        }
    }

}