package com.mskd.flux.features.player.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.mskd.flux.features.player.domain.playback.PendingLaunch
import com.mskd.flux.features.player.domain.playback.PendingLaunchStore

class PendingLaunchStoreImpl(
    private val dataStore: DataStore<Preferences>
) : PendingLaunchStore {

    object Keys {
        val ARTWORK_ID = longPreferencesKey("pending_artwork_id")
        val MEDIA_ID = longPreferencesKey("pending_media_id")
    }

    override suspend fun set(launch: PendingLaunch) {
        dataStore.edit {
            it[Keys.ARTWORK_ID] = launch.artworkId
            it[Keys.MEDIA_ID] = launch.mediaId
        }
    }

    override suspend fun clear() {
        dataStore.edit {
            it.remove(Keys.ARTWORK_ID)
            it.remove(Keys.MEDIA_ID)
        }
    }

    override suspend fun take(): PendingLaunch? {
        var taken: PendingLaunch? = null
        dataStore.edit {
            val artworkId = it[Keys.ARTWORK_ID]
            val mediaId = it[Keys.MEDIA_ID]
            if (artworkId != null && mediaId != null) taken = PendingLaunch(artworkId, mediaId)
            it.remove(Keys.ARTWORK_ID)
            it.remove(Keys.MEDIA_ID)
        }
        return taken
    }

}
