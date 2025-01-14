package com.kaem.flux.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.kaem.flux.ui.theme.Ui
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.internal.toLongOrDefault
import java.util.Locale
import javax.inject.Inject


data class FluxDataStore(
    val lastWatchedIds: List<Long> = listOf(),
    val playerBackwardValue: Int = 10,
    val playerForwardValue: Int = 10,
    val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
    val subtitlesLanguage: Locale = Locale.getDefault()
)


class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) {

    //region Keys

    object Keys {
        val LAST_WATCHED_IDS = stringPreferencesKey("last_watched_ids")
        val LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")
        val PLAYER_BACKWARD = stringPreferencesKey("player_backward")
        val PLAYER_FORWARD = stringPreferencesKey("player_forward")
        val UI_THEME = stringPreferencesKey("ui_theme")
        val SUBTITLES_LANGUAGE = stringPreferencesKey("subtitles_language")
    }

    //endregion

    //region Flow

    val flow: Flow<FluxDataStore> = dataStore.data.map { preferences ->

        val lastWatchedIdsString = preferences[Keys.LAST_WATCHED_IDS] ?: "[]"
        val lastWatchedIds = gson.fromJson<List<Double>>(lastWatchedIdsString, List::class.java)

        val playerBackwardValue = preferences[Keys.PLAYER_BACKWARD]?.toInt() ?: 10
        val playerForwardValue = preferences[Keys.PLAYER_FORWARD]?.toInt() ?: 10

        val uiTheme = preferences[Keys.UI_THEME]?.toString()?.let { Ui.THEME.valueOf(it) } ?: Ui.THEME.SYSTEM

        val subtitlesLanguage = preferences[Keys.SUBTITLES_LANGUAGE]?.toString()?.let { Locale(it) } ?: Locale.getDefault()

        FluxDataStore(
            lastWatchedIds = lastWatchedIds.map { it.toLong() },
            playerBackwardValue = playerBackwardValue,
            playerForwardValue = playerForwardValue,
            uiTheme = uiTheme,
            subtitlesLanguage = subtitlesLanguage
        )
    }

    //endregion

    //region WatchedArtwork

    suspend fun addWatchedArtwork(id: Long) {
        dataStore.edit { preferences ->

            val lastWatchedIdsString = preferences[Keys.LAST_WATCHED_IDS] ?: "[]"
            val lastWatchedIds: ArrayList<Long> = gson.fromJson<ArrayList<Long>>(lastWatchedIdsString, ArrayList::class.java)

            if (lastWatchedIds.none { it == id }) {

                lastWatchedIds.add(0, id)

                preferences[Keys.LAST_WATCHED_IDS] = gson.toJson(lastWatchedIds.take(4))
            }

        }
    }

    suspend fun removeWatchedArtwork(id: Long) {
        dataStore.edit { preferences ->

            val lastWatchedIdsString = preferences[Keys.LAST_WATCHED_IDS] ?: "[]"
            val lastWatchedIds: ArrayList<Long> = gson.fromJson<ArrayList<Long>>(lastWatchedIdsString, ArrayList::class.java)

            lastWatchedIds.remove(id)
            preferences[Keys.LAST_WATCHED_IDS] = gson.toJson(lastWatchedIds.take(4))

        }
    }

    //endregion

    //region Sync time

    fun getSyncTime() : Long = runBlocking {
        dataStore.data.map {
            (it[Keys.LAST_SYNC_TIME] ?: "0").toLongOrDefault(0)
        }.first()
    }

    suspend fun saveSyncTime(syncTime: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_SYNC_TIME] = syncTime.toString()
        }
    }

    //endregion

    //region Player backward/forward


    suspend fun setPlayerBackwardValue(value: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.PLAYER_BACKWARD] = value.toString()
        }
    }


    suspend fun setPlayerForwardValue(value: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.PLAYER_FORWARD] = value.toString()
        }
    }

    fun getPlayerButtonsValues() : Pair<Int, Int> = runBlocking {
        dataStore.data.map {
            (it[Keys.PLAYER_BACKWARD] ?: "0").toInt() to (it[Keys.PLAYER_FORWARD] ?: "0").toInt()
        }.first()
    }

    //endregion

    //region UI Theme

    suspend fun setUiTheme(theme: Ui.THEME) {
        dataStore.edit { preferences ->
            preferences[Keys.UI_THEME] = theme.toString()
        }
    }

    //endregion

    //region Languages

    suspend fun setSubtitlesLanguage(locale: Locale) {
        dataStore.edit { preferences ->
            preferences[Keys.SUBTITLES_LANGUAGE] = locale.language
        }
    }

    fun getSubtitlesLanguage() : Locale = runBlocking {
        dataStore.data.map { preferences ->
            preferences[Keys.SUBTITLES_LANGUAGE]?.let { Locale(it) } ?: Locale.getDefault()
        }.first()
    }

    //endregion

}