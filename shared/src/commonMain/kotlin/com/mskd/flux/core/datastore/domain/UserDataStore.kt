package com.mskd.flux.core.datastore.domain

import kotlinx.coroutines.flow.Flow

interface UserDataStore {

    val flow: Flow<State>

    suspend fun addToRecentlyWatched(artworkId: Long)
    suspend fun removeFromRecentlyWatched(artworkId: Long)
    suspend fun setSyncTime(syncTime: Long)
    suspend fun getSyncTime() : Long
    suspend fun getVersionCode() : Int
    suspend fun setVersionCode(versionCode: Int)
    suspend fun setMessageAsWatched(messageId: Int)
    suspend fun enablePip(enable: Boolean)

    data class State(
        val recentlyWatchedIds: List<Long> = listOf(),
        val watchedMessagesIds: List<Int> = listOf(),
        val syncTime: Long = 0L,
        val versionCode: Int = 0,
        val pipIsEnabled: Boolean = true,
    )

}