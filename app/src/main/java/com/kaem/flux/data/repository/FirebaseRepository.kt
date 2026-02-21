package com.kaem.flux.data.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

class FirebaseRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {

}