package com.mskd.flux.data.useCases.images

import kotlinx.coroutines.flow.Flow

interface ImagesUC {

    val state: Flow<State>
    fun prefetchImages()

    sealed class State {
        data object Idle : State()
        data class InProgress(val progress: Float) : State()
    }

}