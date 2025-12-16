package com.kaem.flux.screens.player

import androidx.compose.runtime.Immutable
import com.kaem.flux.model.media.Media
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Immutable
data class PlayerUiState(
    val media: Media,
    val playerBackward: Long = 10.seconds.inWholeMilliseconds,
    val playerForward: Long = 10.seconds.inWholeMilliseconds,
    val subtitlesLanguage: Locale = Locale.getDefault()
)