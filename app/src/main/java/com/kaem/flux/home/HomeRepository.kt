package com.kaem.flux.home

import com.kaem.flux.layers.LocalLayer
import com.kaem.flux.model.DataState
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val localLayer: LocalLayer
) {

    suspend fun getLocalFiles() = flow {

        emit(DataState.Loading())

        val files = withContext(Dispatchers.Default) {

            localLayer.getLocalFiles()

        }.map { it.name }

        emit(DataState.Success(files))

    }

}