package com.mskd.flux.core.network.tmdb.data.datasource

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.dto.ArtworkDto
import com.mskd.flux.core.network.tmdb.data.dto.MediaTypeDto
import com.mskd.flux.core.network.tmdb.data.dto.TranslationsDto
import com.mskd.flux.core.network.tmdb.data.dto.findWithLocale
import com.mskd.flux.core.network.tmdb.data.dto.genre.GenreDto
import com.mskd.flux.core.network.tmdb.data.dto.movie.MovieDto
import com.mskd.flux.core.network.tmdb.data.dto.show.EpisodeDto
import com.mskd.flux.core.network.tmdb.data.dto.show.SeasonDto
import com.mskd.flux.core.network.tmdb.data.dto.show.ShowDto
import com.mskd.flux.core.network.tmdb.data.service.TMDBService
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.toTmdbFormat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TmdbDataSourceImpl(
    private val tmdbService: TMDBService,
    private val settings: SettingsDataStore
) : TmdbDataSource {

    private companion object {
        const val TAG = "TmdbRepositoryImpl"
    }

    override suspend fun getArtwork(
        file: UserFile
    ): ArtworkDto? {

        val language = settings.getDataLanguage()

        return try {

            val searchResults = if (file.isEpisode) {
                tmdbService.searchShow(
                    title = file.nameProperties.title,
                    year = file.nameProperties.year,
                    language = language.toTmdbFormat()
                )
            } else {
                tmdbService.searchMovie(
                    title = file.nameProperties.title,
                    year = file.nameProperties.year,
                    language = language.toTmdbFormat()
                )
            }

            var tmdbArtwork = searchResults.artworkFor(fileName = file.nameProperties.title)?.also {
                it.type = if (file.isEpisode) MediaTypeDto.SHOW else MediaTypeDto.MOVIE
            }

            // Get translation for show if needed
            if (tmdbArtwork?.type == MediaTypeDto.SHOW && (tmdbArtwork.description.isBlank() || tmdbArtwork.title.isBlank())) {

                getTranslation(
                    request = TranslationRequest.Show(
                        artworkId = tmdbArtwork.id,
                        language = language
                    ),
                )?.let {
                    tmdbArtwork = tmdbArtwork.copy(
                        title = it.data.name ?: tmdbArtwork.title,
                        description = it.data.overview ?: tmdbArtwork.description
                    )
                }

            }

            tmdbArtwork

        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getTmdbArtwork - Fail to get TMDBArtwork for file:${file.name} (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    override suspend fun getGenres(): List<GenreDto> {

        val language = settings.getDataLanguage()

        return coroutineScope {

            val movieGenresDeferred = async { tmdbService.getMovieGenres(language = language.toTmdbFormat()).genres }
            val showGenresDeferred = async { tmdbService.getMovieGenres(language = language.toTmdbFormat()).genres }

            movieGenresDeferred.await() + showGenresDeferred.await()
        }

    }

    override suspend fun getMovie(
        artworkId: Long
    ): MovieDto? {

        val language = settings.getDataLanguage()

        return try {

            var tmdbMovie = tmdbService.getMovieDetails(
                id = artworkId,
                language = language.toTmdbFormat()
            )

            if (tmdbMovie.description.isBlank() || tmdbMovie.title.isBlank()) {

                getTranslation(
                    request = TranslationRequest.Movie(
                        artworkId = artworkId,
                        language = language
                    ),
                )?.let {
                    tmdbMovie = tmdbMovie.copy(
                        title = it.data.name ?: tmdbMovie.title,
                        description = it.data.overview ?: tmdbMovie.description
                    )
                }

            }

            tmdbMovie

        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getTmdbMovie - Fail to get TMDBMovie for artworkId:$artworkId (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    override suspend fun getShow(
        artworkId: Long
    ): ShowDto? {

        val language = settings.getDataLanguage()

        return try {

            var tmdbShow = tmdbService.getShowDetails(
                artworkId = artworkId,
                language = language.toTmdbFormat()
            )

            if (tmdbShow.description.isBlank() || tmdbShow.title.isBlank()) {

                getTranslation(
                    request = TranslationRequest.Show(
                        artworkId = artworkId,
                        language = language
                    ),
                )?.let {
                    tmdbShow = tmdbShow.copy(
                        title = it.data.name ?: tmdbShow.title,
                        description = it.data.overview ?: tmdbShow.description
                    )
                }

            }

            tmdbShow

        } catch (e: Exception) {
            Trace.error(tag = TAG, message = "getTmdbMovie - Fail to get TMDBShow for artworkId:$artworkId (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    override suspend fun getEpisode(
        artworkId: Long,
        season: Int,
        number: Int
    ): EpisodeDto? {

        val language = settings.getDataLanguage()

        return try {

            var tmdbEpisode = tmdbService.getEpisode(
                id = artworkId,
                season = season,
                number = number,
                language = language.toTmdbFormat()
            )

            if (tmdbEpisode.description.isBlank() || tmdbEpisode.title.isBlank()) {

                getTranslation(
                    request = TranslationRequest.Episode(
                        artworkId = artworkId,
                        season = season,
                        number = number,
                        language = language
                    ),
                )?.let {
                    tmdbEpisode = tmdbEpisode.copy(
                        title = it.data.name ?: tmdbEpisode.title,
                        description = it.data.overview ?: tmdbEpisode.description
                    )
                }

            }

            tmdbEpisode

        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getTmdbEpisode - Fail to get TMDBEpisode for artworkId:$artworkId, season:$season, number:$number (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    override suspend fun getSeason(artworkId: Long, season: Int): SeasonDto? {

        val language = settings.getDataLanguage()

        return try {

            var tmdbSeason = tmdbService.getSeason(
                id = artworkId,
                season = season,
                language = language.toTmdbFormat()
            )

            if (tmdbSeason.description.isBlank() || tmdbSeason.title.isBlank()) {

                getTranslation(
                    request = TranslationRequest.Season(
                        artworkId = artworkId,
                        season = season,
                        language = language
                    ),
                )?.let {
                    tmdbSeason = tmdbSeason.copy(
                        title = it.data.name ?: tmdbSeason.title,
                        description = it.data.overview ?: tmdbSeason.description
                    )
                }

            }

            tmdbSeason

        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getTmdbSeason - Fail to get TMDBSeason for artworkId:$artworkId, season:$season (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    override suspend fun getTranslation(request: TranslationRequest): TranslationsDto.Translation? {

        return try {

            val result = when (request) {
                is TranslationRequest.Movie -> tmdbService.getMovieTranslations(artworkId = request.artworkId)
                is TranslationRequest.Show -> tmdbService.getShowTranslations(artworkId = request.artworkId)
                is TranslationRequest.Season -> tmdbService.getSeasonTranslations(artworkId = request.artworkId, season = request.season)
                is TranslationRequest.Episode -> tmdbService.getEpisodeTranslations(artworkId = request.artworkId, season = request.season, number = request.number)
            }

            result.translations.findWithLocale(request.language)

        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getTmdbTranslations - Fail to get translations for $request", throwable = e)
            null
        }

    }

}