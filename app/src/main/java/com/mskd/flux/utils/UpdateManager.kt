package com.mskd.flux.utils

object UpdateManager {

    private val fullSyncForVersions = listOf(
        21
    )

    fun fullSyncIsNeeded(lastSyncVersionCode: Int, currentVersionCode: Int) : Boolean {
        return lastSyncVersionCode < currentVersionCode
                && lastSyncVersionCode < (fullSyncForVersions.maxOrNull() ?: currentVersionCode)
                && currentVersionCode >= (fullSyncForVersions.minOrNull() ?: 0
        )
    }
}