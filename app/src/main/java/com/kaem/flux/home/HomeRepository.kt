package com.kaem.flux.home

import com.kaem.flux.data.source.FilesDataSource
import com.kaem.flux.data.source.LocalFilesDataSource
import com.kaem.flux.model.DataState
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val localFilesDataSource: FilesDataSource
) {

    suspend fun getLocalFiles() = flow {

        emit(DataState.Loading())

        val test = localFilesDataSource.getFiles()

        emit(DataState.Success(test))

    }

}