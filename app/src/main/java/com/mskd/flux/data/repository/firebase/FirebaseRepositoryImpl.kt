package com.kaem.flux.data.repository.firebase

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kaem.flux.BuildConfig
import com.kaem.flux.model.remoteConfig.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val gson: Gson
) : FirebaseRepository {

    private val _message = MutableStateFlow<Message?>(null)
    override val message: StateFlow<Message?> = _message.asStateFlow()

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

    override suspend fun fetchMessages() {

        try {

            remoteConfig.fetchAndActivate().await()
            val changelogString = remoteConfig.getString("messages")

            // Retrieve all messages
            val type = object : TypeToken<ArrayList<Message>>() {}.type
            val allMessages: List<Message> = gson.fromJson(changelogString, type)
            val sortedMessages = allMessages.sortedByDescending { it.versionCode }

            // Filter message
            val version = BuildConfig.VERSION_CODE
            val filteredMessage = sortedMessages.find { it.versionCode == version } ?: sortedMessages.find { it.versionCode < 0 }
            _message.update { filteredMessage }

        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Fail to fetch messages", e)
        }

    }

}