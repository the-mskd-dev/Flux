package com.kaem.flux.home

import com.kaem.flux.model.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HomeRepository @Inject constructor() {

    suspend fun getLocalFiles() = flow {

        emit(DataState.Loading())

        withContext(Dispatchers.Default) {

            delay(2000L)

        }

        val test = listOf("Test 1", "Test 2", "Test 3", "Test 4", "Test 5", "Test 6")

        emit(DataState.Success(test))

    }
}