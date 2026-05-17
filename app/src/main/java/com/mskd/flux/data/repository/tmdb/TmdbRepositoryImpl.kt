package com.mskd.flux.data.repository.tmdb

import android.util.Log
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.tmdb.TMDBArtwork
import com.mskd.flux.model.tmdb.TMDBEpisode
import com.mskd.flux.model.tmdb.TMDBMediaType
import com.mskd.flux.model.tmdb.TMDBMovie
import com.mskd.flux.model.tmdb.TMDBSeason
import com.mskd.flux.model.tmdb.TMDBTranslations
import com.mskd.flux.model.tmdb.findWithLocale
import com.mskd.flux.utils.extensions.toTmdbFormat
import java.util.Locale
import javax.inject.Inject

class TmdbRepositoryImpl @Inject constructor(
    private val tmdbService: TMDBService,
    private val settings: SettingsRepository
) : TmdbRepository {

    private companion object {
        const val TAG = "TmdbRepositoryImpl"
    }

    override suspend fun getTmdbArtwork(
        file: UserFile
    ): TMDBArtwork? {

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
                it.type = if (file.isEpisode) TMDBMediaType.SHOW else TMDBMediaType.MOVIE
            }

            // Get translation for show if needed
            if (tmdbArtwork?.type == TMDBMediaType.SHOW && (tmdbArtwork.description.isBlank() || tmdbArtwork.title.isBlank())) {

                getTmdbTranslations(
                    request = TMDBTranslations.Request.Show(
                        artworkId = tmdbArtwork.id,
                    ),
                    language = language
                )?.let {
                    tmdbArtwork = tmdbArtwork.copy(
                        title = it.data.name ?: tmdbArtwork.title,
                        description = it.data.overview ?: tmdbArtwork.description
                    )
                }

            }

            tmdbArtwork

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbArtwork - Fail to get TMDBArtwork for file:${file.name} (${language.toTmdbFormat()})", e)
            null
        }

    }

    override suspend fun getTmdbMovie(
        artworkId: Long
    ): TMDBMovie? {

        val language = settings.getDataLanguage()

        return try {

            var tmdbMovie = tmdbService.getMovieDetails(
                id = artworkId,
                language = language.toTmdbFormat()
            )

            if (tmdbMovie.description.isBlank() || tmdbMovie.title.isBlank()) {

                getTmdbTranslations(
                    request = TMDBTranslations.Request.Movie(
                        artworkId = artworkId
                    ),
                    language = language
                )?.let {
                    tmdbMovie = tmdbMovie.copy(
                        title = it.data.name ?: tmdbMovie.title,
                        description = it.data.overview ?: tmdbMovie.description
                    )
                }

            }

            tmdbMovie

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbMovie - Fail to get TMDBMovie for artworkId:$artworkId (${language.toTmdbFormat()})", e)
            null
        }

    }

    override suspend fun getTmdbEpisode(
        artworkId: Long,
        season: Int,
        number: Int
    ): TMDBEpisode? {

        val language = settings.getDataLanguage()

        return try {

            var tmdbEpisode = tmdbService.getEpisode(
                id = artworkId,
                season = season,
                number = number,
                language = language.toTmdbFormat()
            ).also {
                it.artworkId = artworkId
            }

            if (tmdbEpisode.description.isBlank() || tmdbEpisode.title.isBlank()) {

                getTmdbTranslations(
                    request = TMDBTranslations.Request.Episode(
                        artworkId = artworkId,
                        season = season,
                        number = number
                    ),
                    language = language
                )?.let {
                    tmdbEpisode = tmdbEpisode.copy(
                        title = it.data.name ?: tmdbEpisode.title,
                        description = it.data.overview ?: tmdbEpisode.description
                    )
                }

            }

            tmdbEpisode

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbEpisode - Fail to get TMDBEpisode for artworkId:$artworkId, season:$season, number:$number (${language.toTmdbFormat()})", e)
            null
        }

    }

    override suspend fun getTmdbSeason(artworkId: Long, season: Int): TMDBSeason? {

        val language = settings.getDataLanguage()

        return try {

            var tmdbSeason = tmdbService.getSeason(
                id = artworkId,
                season = season,
                language = language.toTmdbFormat()
            )

            if (tmdbSeason.description.isBlank() || tmdbSeason.title.isBlank()) {

                getTmdbTranslations(
                    request = TMDBTranslations.Request.Season(
                        artworkId = artworkId,
                        season = season
                    ),
                    language = language
                )?.let {
                    tmdbSeason = tmdbSeason.copy(
                        title = it.data.name ?: tmdbSeason.title,
                        description = it.data.overview ?: tmdbSeason.description
                    )
                }

            }

            tmdbSeason

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbSeason - Fail to get TMDBSeason for artworkId:$artworkId, season:$season (${language.toTmdbFormat()})", e)
            null
        }

    }

    override suspend fun getTmdbTranslations(request: TMDBTranslations.Request, language: Locale): TMDBTranslations.Translation? {

        return try {

            val result = when (request) {
                is TMDBTranslations.Request.Movie -> tmdbService.getMovieTranslations(artworkId = request.artworkId)
                is TMDBTranslations.Request.Show -> tmdbService.getShowTranslations(artworkId = request.artworkId)
                is TMDBTranslations.Request.Season -> tmdbService.getSeasonTranslations(artworkId = request.artworkId, season = request.season)
                is TMDBTranslations.Request.Episode -> tmdbService.getEpisodeTranslations(artworkId = request.artworkId, season = request.season, number = request.number)
            }

            result.translations.findWithLocale(language)

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbTranslations - Fail to get translations for $request", e)
            null
        }

    }

}