package com.mskd.flux.core.network.tmdb.data.datasource

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.dto.ArtworkDto
import com.mskd.flux.core.network.tmdb.data.dto.EpisodeDto
import com.mskd.flux.core.network.tmdb.data.dto.MediaTypeDto
import com.mskd.flux.core.network.tmdb.data.dto.MovieDto
import com.mskd.flux.core.network.tmdb.data.dto.SeasonDto
import com.mskd.flux.core.network.tmdb.data.dto.TranslationsDto
import com.mskd.flux.core.network.tmdb.data.dto.findWithLocale
import com.mskd.flux.core.network.tmdb.data.service.TMDBService
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.utils.extensions.toTmdbFormat
import io.github.aakira.napier.Napier
import java.util.Locale

class TmdbDataSourceImpl(
    private val tmdbService: TMDBService,
    private val settings: SettingsDataStore
) : TmdbDataSource {

    private companion object {
        const val TAG = "TmdbRepositoryImpl"
    }

    override suspend fun getTmdbArtwork(
        file: UserFile
    ): ArtworkDto? {

        val language = settings.getDataLanguage()

        return try {

            val tmdbArtworks = if (file.isEpisode) {
                tmdbService.getShow(
                    title = file.nameProperties.title,
                    year = file.nameProperties.year,
                    language = language.toTmdbFormat()
                )
            } else {
                tmdbService.getMovie(
                    title = file.nameProperties.title,
                    year = file.nameProperties.year,
                    language = language.toTmdbFormat()
                )
            }

            var tmdbArtwork = tmdbArtworks.artworkFor(fileName = file.nameProperties.title)?.also {
                it.type = if (file.isEpisode) MediaTypeDto.SHOW else MediaTypeDto.MOVIE
            }

            // Get translation for show if needed
            if (tmdbArtwork?.type == MediaTypeDto.SHOW && (tmdbArtwork.description.isBlank() || tmdbArtwork.title.isBlank())) {

                getTmdbTranslation(
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

    override suspend fun getTmdbMovie(
        artworkId: Long
    ): MovieDto? {

        val language = settings.getDataLanguage()

        return try {

            var tmdbMovie = tmdbService.getMovieDetails(
                id = artworkId,
                language = language.toTmdbFormat()
            )

            if (tmdbMovie.description.isBlank() || tmdbMovie.title.isBlank()) {

                getTmdbTranslation(
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

    override suspend fun getTmdbEpisode(
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

                tmdbEpisode = translateTmdbEpisode(
                    artworkId = artworkId,
                    episodeDto = tmdbEpisode,
                    language = language
                )

            }

            tmdbEpisode

        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getTmdbEpisode - Fail to get TMDBEpisode for artworkId:$artworkId, season:$season, number:$number (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    override suspend fun getTmdbSeason(artworkId: Long, season: Int): SeasonDto? {

        val language = settings.getDataLanguage()

        return try {

            var tmdbSeason = tmdbService.getSeason(
                id = artworkId,
                season = season,
                language = language.toTmdbFormat()
            )

            if (tmdbSeason.description.isBlank() || tmdbSeason.title.isBlank()) {

                getTmdbTranslation(
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

    override suspend fun translateTmdbEpisode(artworkId: Long, episodeDto: EpisodeDto, language: Locale): EpisodeDto {

        return getTmdbTranslation(
            request = TranslationRequest.Episode(
                artworkId = artworkId,
                season = episodeDto.season,
                number = episodeDto.number,
                language = language
            ),
        )?.let {
            episodeDto.copy(
                title = it.data.name ?: episodeDto.title,
                description = it.data.overview ?: episodeDto.description
            )
        } ?: episodeDto

    }

    override suspend fun getTmdbTranslation(request: TranslationRequest): TranslationsDto.Translation? {

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