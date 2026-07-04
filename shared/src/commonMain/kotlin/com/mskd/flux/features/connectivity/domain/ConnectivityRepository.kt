package com.mskd.flux.features.connectivity.domain

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.Flow

interface ConnectivityRepository {
    val isOnline: Flow<Boolean>
    fun currentlyOnline(): Boolean
}

val LocalConnectivity = compositionLocalOf { false }