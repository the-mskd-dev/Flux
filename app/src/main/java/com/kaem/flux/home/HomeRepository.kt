package com.kaem.flux.home

import android.util.Log
import com.kaem.flux.data.source.FilesDataSource
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.model.FileNameProperties
import com.kaem.flux.model.flux.FluxArtwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val localFilesDataSource: FilesDataSource,
    private val tmdbService: TMDBService
) {

    suspend fun getArtworks() : Flow<Result<List<FluxArtwork>>> = flow {

        val localFiles = localFilesDataSource.getFiles()

        //emit(Result.success(localFiles))

        val test1 = FileNameProperties.fromFileName("spy x family_s02e03.mkv")
        val test2 = FileNameProperties.fromFileName("spiderman (2002).mkv")
        val test3 = FileNameProperties.fromFileName("naruto.mkv")
        val test4 = FileNameProperties.fromFileName("spiderman (2017)_s02e04.mkv")
        val test5 = FileNameProperties.fromFileName("spiderman_2017.mkv")
        val test6 = FileNameProperties.fromFileName("2001 : L'odyssée de l'espace.mkv")

        val result = listOf(test1, test2, test3, test4, test5, test6)
        Log.d("TEST", result.toString())

    }

}