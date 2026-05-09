package com.mskd.flux.data.repository.tmdb

import android.util.Log
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.tmdb.TMDBArtwork
import com.mskd.flux.model.tmdb.TMDBEpisode
import com.mskd.flux.model.tmdb.TMDBMediaType
import com.mskd.flux.model.tmdb.TMDBMovie
import kotlinx.coroutines.delay
import retrofit2.HttpException
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

            tmdbArtworks.results.first().also {
                it.type = if (file.isEpisode) TMDBMediaType.SHOW else TMDBMediaType.MOVIE
            }

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbArtwork - Fail to get TMDBArtwork for file:${file.name}", e)
            null
        }

    }

    override suspend fun getTmdbMovie(
        artworkId: Long
    ): TMDBMovie? {

        return try {

            tmdbService.getMovieDetails(id = artworkId)

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

            tmdbService.getEpisode(
                id = artworkId,
                season = season,
                episode = number
            ).also {
                it.artworkId = artworkId
            }

        } catch (e: Exception) {
            Log.e(TAG, "getTmdbEpisode - Fail to get TMDBEpisode for artworkId:$artworkId, season:$season, number:$number", e)
            null
        }

    }
}