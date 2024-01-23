package com.kaem.flux.home

import android.util.Log
import com.kaem.flux.layers.LocalService
import com.kaem.flux.model.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val localService: LocalService
) {

    suspend fun getLocalFiles() = flow {

        emit(DataState.Loading())

        val test = localService.getLocalFiles()


        emit(DataState.Success(test))

    }

}