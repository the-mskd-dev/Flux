package com.kaem.flux.data.repository

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kaem.flux.BuildConfig
import com.kaem.flux.model.remoteConfig.Message
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val gson: Gson
) {

    private val _changelog = MutableStateFlow<List<Message>>(emptyList())
    val changelog = _changelog.asStateFlow()

    init {
        initRemoteConfig()
    }

    private fun initRemoteConfig() {

        Log.d("FirebaseRepository", "initRemoteConfig")

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 1 else 3600
        }

        remoteConfig.setConfigSettingsAsync(configSettings)

    }

    suspend fun fetchChangelog() {

        try {

            remoteConfig.fetchAndActivate().await()
            val changelogString = remoteConfig.getString("changelog")

            val type = object : TypeToken<ArrayList<Message>>() {}.type
            val fullChangelog: ArrayList<Message> = gson.fromJson(changelogString, type)
            _changelog.update { fullChangelog }

        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Fail to fetch changelog", e)
        }

    }

}