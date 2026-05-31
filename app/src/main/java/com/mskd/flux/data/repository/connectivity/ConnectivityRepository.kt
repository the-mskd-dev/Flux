package com.mskd.flux.data.repository.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityRepository {
    val isOnline: Flow<Boolean>
    fun currentlyOnline(): Boolean
}