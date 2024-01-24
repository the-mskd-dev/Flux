package com.kaem.flux.home

import android.util.Log
import com.kaem.flux.data.source.FilesDataSource
import com.kaem.flux.data.tmdb.TMDBService
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val localFilesDataSource: FilesDataSource,
    private val tmdbService: TMDBService
) {

    suspend fun getArtworks() = flow {

        val test = localFilesDataSource.getFiles()

        emit(Result.success(test))

        val result = tmdbService.authenticate()

        Log.d("TEST", "Result: $result")

    }

}