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


    override suspend fun getTmdbArtwork(
        file: UserFile
    ): TMDBArtwork?  = withRetry {

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

    }

    override suspend fun getTmdbMovie(
        artworkId: Long
    ): TMDBMovie? = withRetry {

        tmdbService.getMovieDetails(id = artworkId)

    }

    override suspend fun getTmdbEpisode(
        artworkId: Long,
        season: Int,
        number: Int
    ): TMDBEpisode? = withRetry {

        tmdbService.getEpisode(
            id = artworkId,
            season = season,
            episode = number
        ).also {
            it.artworkId = artworkId
        }

    }

    private suspend fun <T> withRetry(
        retryCount: Int = 0,
        block: suspend () -> T
    ): T? {
        return try {
            block()
        } catch (e: HttpException) {
            if (e.code() == 429 && retryCount < 3) {
                delay(1000L * (retryCount + 1))
                withRetry(retryCount + 1, block)
            } else null
        } catch (e: Exception) {
            Log.e("TmdbRepositoryImpl", "Request failed", e)
            null
        }
    }

}