package com.mskd.flux.data.repository.tmdb

import android.util.Log
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.tmdb.TMDBArtwork
import com.mskd.flux.model.tmdb.TMDBEpisode
import com.mskd.flux.model.tmdb.TMDBMediaType
import com.mskd.flux.model.tmdb.TMDBMovie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TmdbRepositoryImpl @Inject constructor(
    private val tmdbService: TMDBService,
) : TmdbRepository {

    private val dispatcher = Dispatchers.IO.limitedParallelism(10)


    override suspend fun getTmdbArtwork(file: UserFile): TMDBArtwork? = withContext(dispatcher) {

        try {

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
            Log.e("TmdbRepositoryImpl", "[getTmdbArtworks] Fail to get artworks from tmdb for ${file.nameProperties.title}", e)
            null
        }

    }

    override suspend fun getTmdbMovie(artworkId: Long): TMDBMovie? = withContext(dispatcher) {

        try {

            tmdbService.getMovieDetails(id = artworkId)

        } catch (e: Exception) {
            Log.e("TmdbRepositoryImpl", "[getTmdbMovie] Fail to get movie from tmdb for id $artworkId", e)
            null
        }

    }

    override suspend fun getTmdbEpisode(
        artworkId: Long,
        season: Int,
        number: Int
    ): TMDBEpisode? = withContext(dispatcher) {

        try {

            tmdbService.getEpisode(
                id = artworkId,
                season = season,
                episode = number
            ).also {
                it.artworkId = artworkId
            }

        } catch (e: Exception) {
            Log.e("TmdbRepositoryImpl", "[getTmdbEpisode] Fail to get episode from tmdb for id $artworkId, season $season, number $number", e)
            null
        }

    }

}