package com.mskd.flux.features.privateFolder.domain.datastore

import kotlinx.coroutines.flow.Flow

interface PrivateFolderDataStore {

    val flow: Flow<State>

    suspend fun setEnabled(enabled: Boolean)

    suspend fun setIncludeNsfw(includeNsfw: Boolean)

    suspend fun setPin(pin: String)

    suspend fun verifyPin(pin: String): Boolean

    data class State(
        val enabled: Boolean = false,
        val includeNsfw: Boolean = true
    )

}
