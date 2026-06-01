package com.mskd.flux.data.repository.connectivity

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.Flow

interface ConnectivityRepository {
    val isOnline: Flow<Boolean>
    fun currentlyOnline(): Boolean
}

val LocalConnectivity = compositionLocalOf { false }