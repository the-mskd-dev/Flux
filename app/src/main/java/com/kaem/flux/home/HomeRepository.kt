package com.kaem.flux.home

import com.kaem.flux.data.source.FilesDataSource
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val localFilesDataSource: FilesDataSource
) {

    suspend fun getLocalFiles() = flow {

        val test = localFilesDataSource.getFiles()

        emit(Result.success(test))

    }

}