package com.kaem.flux.data.repository

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import javax.inject.Inject

class FirebaseRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val gson: Gson
) {

    init {
        initRemoteConfig()
        testRemoteConfig()
    }

    private fun initRemoteConfig() {

        Log.d("FirebaseRepository", "initRemoteConfig")

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }

        remoteConfig.setConfigSettingsAsync(configSettings)

    }

    private fun testRemoteConfig() {

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = remoteConfig.getString("test")
                    Log.d("FirebaseRepository", "testRemoteConfig: $result")
                } else {
                    Log.e("FirebaseRepository", "testRemoteConfig: fail to get value")
                }
            }
    }

}