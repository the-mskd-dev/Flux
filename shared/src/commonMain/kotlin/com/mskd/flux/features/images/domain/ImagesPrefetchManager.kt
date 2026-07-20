package com.mskd.flux.features.images.domain

import kotlinx.coroutines.flow.Flow

interface ImagesPrefetchManager {

    val state: Flow<State>
    fun prefetchImages()

    sealed class State {
        data object Idle : State()
        data class InProgress(val progress: Float) : State()
    }

}