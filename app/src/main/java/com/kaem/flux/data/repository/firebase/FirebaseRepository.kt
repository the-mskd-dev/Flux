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

interface FirebaseRepository {

    val message: StateFlow<Message?>

    suspend fun fetchMessages()

}