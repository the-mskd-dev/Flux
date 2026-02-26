package com.kaem.flux.data.repository.user

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.kaem.flux.data.repository.user.UserRepositoryImpl.Keys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.IOException

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