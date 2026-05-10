package com.mskd.flux.data.repository.tmdb

import android.util.Log
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.tmdb.TMDBArtwork
import com.mskd.flux.model.tmdb.TMDBEpisode
import com.mskd.flux.model.tmdb.TMDBMediaType
import com.mskd.flux.model.tmdb.TMDBMovie
import com.mskd.flux.model.tmdb.TMDBTranslations
import com.mskd.flux.model.tmdb.findEnglish
import javax.inject.Inject

class TmdbRepositoryImpl @Inject constructor(
    private val tmdbService: TMDBService,
) : TmdbRepository {

    private companion object {
        const val TAG = "TmdbRepositoryImpl"
    }


    override suspend fun getTmdbArtwork(
        file: UserFile
    ): TMDBArtwork? {

        return try {

            val tmdbArtworks = if (file.isEpisode) {
                tmdbService.getShow(
                    title = file.nameProperties.title,
                    year = file.nameProperties.year
                )
            } else {
                tmdbService.getMovie(
                    title = file.nameProperties.title,
                    year = file.nameProperties.year
                )
            }

            var tmdbArtwork = tmdbArtworks.results.first().also {
                it.type = if (file.isEpisode) TMDBMediaType.SHOW else TMDBMediaType.MOVIE
            }

            // Get translation for show if needed
            if (tmdbArtwork.type == TMDBMediaType.SHOW && (tmdbArtwork.description.isBlank() || tmdbArtwork.title.isBlank())) {

                getTmdbShowTranslations(
                    artworkId = tmdbArtwork.id
                ).findEnglish()?.let {
                    tmdbArtwork = tmdbArtwork.copy(
                        title = it.data.name,
                        description = it.data.overview
                    )
                }

            }

            tmdbArtwork

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbArtwork - Fail to get TMDBArtwork for file:${file.name}", e)
            null
        }

    }

    override suspend fun getTmdbMovie(
        artworkId: Long
    ): TMDBMovie? {

        return try {

            var tmdbMovie = tmdbService.getMovieDetails(id = artworkId)

            if (tmdbMovie.description.isBlank() || tmdbMovie.title.isBlank()) {

                getTmdbMovieTranslations(
                    artworkId = artworkId
                ).findEnglish()?.let {
                    tmdbMovie = tmdbMovie.copy(
                        title = it.data.name,
                        description = it.data.overview
                    )
                }

            }

            tmdbMovie

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbMovie - Fail to get TMDBMovie for artworkId:$artworkId", e)
            null
        }

    }

    override suspend fun getTmdbEpisode(
        artworkId: Long,
        season: Int,
        number: Int
    ): TMDBEpisode? {

        return try {

            var tmdbEpisode = tmdbService.getEpisode(
                id = artworkId,
                season = season,
                episode = number
            ).also {
                it.artworkId = artworkId
            }

            if (tmdbEpisode.description.isBlank() || tmdbEpisode.title.isBlank()) {

                getTmdbEpisodeTranslations(
                    artworkId = artworkId,
                    season = season,
                    number = number
                ).findEnglish()?.let {
                    tmdbEpisode = tmdbEpisode.copy(
                        title = it.data.name,
                        description = it.data.overview
                    )
                }

            }

            tmdbEpisode

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbEpisode - Fail to get TMDBEpisode for artworkId:$artworkId, season:$season, number:$number", e)
            null
        }

    }

    override suspend fun getTmdbMovieTranslations(artworkId: Long): List<TMDBTranslations.Translation> {

        return try {

            tmdbService
                .getMovieTranslations(id = artworkId)
                .translations

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbMovieTranslations - Fail to get translations for movie (artworkId:$artworkId)", e)
            emptyList()
        }

    }

    override suspend fun getTmdbShowTranslations(artworkId: Long): List<TMDBTranslations.Translation> {

        return try {

            tmdbService
                .getShowTranslations(id = artworkId)
                .translations

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbShowTranslations - Fail to get translations for show (artworkId:$artworkId)", e)
            emptyList()
        }

    }

    override suspend fun getTmdbEpisodeTranslations(
        artworkId: Long,
        season: Int,
        number: Int
    ): List<TMDBTranslations.Translation> {

        return try {

            tmdbService
                .getShowTranslations(id = artworkId)
                .translations

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbEpisodeTranslations - Fail to get translations for episode (artworkId:$artworkId, season:$season, number:$number)", e)
            emptyList()
        }

    }


}