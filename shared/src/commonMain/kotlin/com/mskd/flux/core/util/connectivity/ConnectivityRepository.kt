package com.mskd.flux.core.util.connectivity

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.Flow

interface ConnectivityRepository {
    val isOnline: Flow<Boolean>
    fun currentlyOnline(): Boolean
}

val LocalConnectivity = compositionLocalOf { false }