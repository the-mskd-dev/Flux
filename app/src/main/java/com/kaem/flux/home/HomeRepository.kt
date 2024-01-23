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

        Log.d("TEST", "start getLocalFiles")

        emit(DataState.Loading())

        val test = withContext(Dispatchers.Default) {

            localService.getLocalFiles()

        }

        Log.d("TEST", "end getLocalFiles(${test.size})")

        emit(DataState.Success(test))

    }

}