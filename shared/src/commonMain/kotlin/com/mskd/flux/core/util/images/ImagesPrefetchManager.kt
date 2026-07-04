package com.mskd.flux.core.util.images

import kotlinx.coroutines.flow.Flow

interface ImagesPrefetchManager {

    val state: Flow<State>
    fun prefetchImages()

    sealed class State {
        data object Idle : State()
        data class InProgress(val progress: Float) : State()
    }

}