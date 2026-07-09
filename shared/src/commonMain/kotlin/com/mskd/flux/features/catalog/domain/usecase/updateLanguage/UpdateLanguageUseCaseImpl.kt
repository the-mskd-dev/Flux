package com.mskd.flux.features.catalog.domain.usecase.updateLanguage

import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.core.domain.model.artwork.ContentType
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.features.tmdb.data.dto.TranslationsDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class UpdateLanguageUseCaseImpl(
    private val tmdb: TmdbDataSource,
    private val database: DatabaseRepository,
    private val settings: SettingsDataStore,
    private val coordinator: CatalogSyncCoordinator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(10)
) : UpdateLanguageUseCase {

    override fun invoke() {
        coordinator.launch(full = false) {

            val language = settings.getDataLanguage()
            val shows = database.getArtworks().filter { it.type == ContentType.SHOW }
            val movies = database.getMovies()
            val seasons = database.getSeasons()
            val episodes = database.getEpisodes()

            val batchSize = 25

            supervisorScope {

                // Movies
                launch(dispatcher) {
                    movies.chunked(batchSize).forEach { chunk ->
                        val translated = chunk.map { movie ->
                            async {
                                tmdb.getTmdbTranslation(
                                    request = TranslationsDto.Request.Movie(artworkId = movie.artworkId, language = language)
                                )?.let { translation ->
                                    movie.copy(
                                        title = translation.data.name ?: movie.title,
                                        description = translation.data.overview ?: movie.description
                                    )
                                }
                            }
                        }.awaitAll().filterNotNull()

                        if (translated.isNotEmpty()) database.saveMovies(translated)
                    }
                }

                // Shows
                launch(dispatcher) {
                    shows.chunked(batchSize).forEach { chunk ->
                        val translated = chunk.map { show ->
                            async {
                                tmdb.getTmdbTranslation(
                                    request = TranslationsDto.Request.Show(artworkId = show.id, language = language)
                                )?.let { translation ->
                                    show.copy(
                                        title = translation.data.name ?: show.title,
                                        description = translation.data.overview ?: show.description
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
                                tmdb.getTmdbTranslation(
                                    request = TranslationsDto.Request.Season(artworkId = season.artworkId, season = season.season, language = language)
                                )?.let { translation ->
                                    season.copy(
                                        title = translation.data.name ?: season.title,
                                        description = translation.data.overview ?: season.description
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
                                tmdb.getTmdbTranslation(
                                    request = TranslationsDto.Request.Episode(artworkId = episode.artworkId, season = episode.season, number = episode.number, language = language)
                                )?.let { translation ->
                                    episode.copy(
                                        title = translation.data.name ?: episode.title,
                                        description = translation.data.overview ?: episode.description
                                    )
                                }
                            }
                        }.awaitAll().filterNotNull()

                        if (translated.isNotEmpty()) database.saveEpisodes(translated)
                    }
                }


            }

        }
    }

}