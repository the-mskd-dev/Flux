package com.mskd.flux.features.player.domain.model

import com.mskd.flux.core.model.artwork.Media
import kotlinx.serialization.Serializable

@Serializable
data class PlayerParams(
    val mediaId: Long,
    val artworkId: Long? = null
) {

    companion object {

        fun fromMedia(media: Media) = PlayerParams(
            mediaId = media.mediaId,
            artworkId = media.artworkId
        )
    }
    
}