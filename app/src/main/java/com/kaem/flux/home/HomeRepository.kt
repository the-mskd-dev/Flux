package com.kaem.flux.home

import android.util.Log
import com.kaem.flux.data.source.FilesDataSource
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.model.FileNameProperties
import com.kaem.flux.model.flux.FluxArtwork
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.model.tmdb.TMDBMediaType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val localFilesDataSource: FilesDataSource,
    private val tmdbService: TMDBService
) {

    suspend fun getArtworks() : Flow<Result<List<TMDBArtwork>>> = flow {

        val localFiles = localFilesDataSource.getFiles()

        val result = buildList {

            for (files in localFiles) {

                fileToTmdbArtwork(files.nameProperties)?.let { add(it) }

            }

        }

        emit(Result.success(result))

    }

    private suspend fun fileToTmdbArtwork(fileNameProperties: FileNameProperties) : TMDBArtwork? {

        return if (fileNameProperties.episode != null && fileNameProperties.season != null) {

            val artworks = tmdbService.getShow(
                title = fileNameProperties.title,
                year = fileNameProperties.year
            )

            artworks.results.maxBy { it.popularity }

        } else {

            val artworks = tmdbService.getMovie(
                title = fileNameProperties.title,
                year = fileNameProperties.year
            )

            artworks.results.firstOrNull()

        }

    }

    private suspend fun tmdbToFluxArtwork(tmdbArtwork: TMDBArtwork) : FluxArtwork? {

        when (tmdbArtwork.type){

            TMDBMediaType.MOVIE -> {

            }

            TMDBMediaType.SHOW -> {

            }
            else -> {}
        }


        return null
    }

}