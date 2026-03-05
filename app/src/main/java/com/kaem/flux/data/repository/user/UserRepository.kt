package com.kaem.flux.data.repository.user

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow

val Context.userDataStore by preferencesDataStore(
    name ="UserDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

data class UserPreferences(
    val recentlyWatchedIds: List<Long> = listOf(),
    val watchedMessagesIds: List<Int> = listOf(),
    val syncTime: Long = 0L
)

interface UserRepository {

    val flow: Flow<UserPreferences>

    suspend fun addToRecentlyWatched(artworkId: Long)

    suspend fun removeFromRecentlyWatched(artworkId: Long)

    suspend fun setSyncTime(syncTime: Long)

    suspend fun getSyncTime() : Long

    suspend fun setMessageAsWatched(messageId: Int)

}