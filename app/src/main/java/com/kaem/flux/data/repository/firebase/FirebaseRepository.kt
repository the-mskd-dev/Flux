package com.kaem.flux.data.repository.firebase

import com.kaem.flux.model.remoteConfig.Message
import kotlinx.coroutines.flow.StateFlow

interface FirebaseRepository {

    val message: StateFlow<Message?>

    suspend fun fetchMessages()

}