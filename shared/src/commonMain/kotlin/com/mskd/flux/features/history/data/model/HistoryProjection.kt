package com.mskd.flux.features.history.data.model

import androidx.room.Embedded
import com.mskd.flux.core.database.data.model.MediaEntity

data class HistoryProjection(
    @Embedded val history: HistoryEntity,
    @Embedded val media: MediaEntity
)
