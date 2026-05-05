package com.mskd.flux.data.repository.tmdb

import android.util.Log
import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.tmdb.TMDBArtwork
import javax.inject.Inject

class TmdbRepositoryImpl @Inject constructor(
    private val db: DatabaseDao,
    private val tmdbService: TMDBService,
) : TmdbRepository {

    override suspend fun searchTmdbArtworks(file: UserFile): List<TMDBArtwork> {

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

            tmdbArtworks.results

        } catch (e: Exception) {
            Log.e("TmdbRepositoryImpl", "Fail to get artworks from tmdb for ${file.nameProperties.title}", e)
            emptyList()
        }

    }

    override suspend fun applyTmdbArtwork(
        artwork: Artwork,
        tmdbArtwork: TMDBArtwork
    ) {

        if (!artwork.type.equalsTmdb(tmdbType = tmdbArtwork.type))
            return

        TODO("Not yet implemented")

    }

}