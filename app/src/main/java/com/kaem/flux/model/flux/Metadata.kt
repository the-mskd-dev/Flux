package com.kaem.flux.model.flux

import androidx.annotation.OptIn
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi

sealed class Metadata(
    val name: String,
    val id: String,
    val language: String
) {

    class Audio(
        name: String,
        id: String,
        language: String
    ) : Metadata(
        name = name,
        id = id,
        language = language
    )

    class Subtitles(
        name: String,
        id: String,
        language: String
    ) : Metadata(
        name = name,
        id = id,
        language = language
    )

    companion object {

        @OptIn(UnstableApi::class)
        fun tracksToParameters(tracks: Tracks) : List<Metadata> {
            return buildList {

                tracks.groups.forEach { track ->

                    track.mediaTrackGroup.getFormat(0).let { format ->

                        val sampleMimeType = format.sampleMimeType.orEmpty()
                        val codecs = format.codecs.orEmpty()
                        when {
                            sampleMimeType.contains("audio", true) -> {
                                add(
                                    Audio(
                                        name = format.label.orEmpty(),
                                        id = format.id.orEmpty(),
                                        language = format.language.orEmpty()
                                    )
                                )
                            }
                            codecs.contains("text", true) -> {
                                add(
                                    Subtitles(
                                        name = format.label.orEmpty(),
                                        id = format.id.orEmpty(),
                                        language = format.language.orEmpty()
                                    )
                                )
                            }
                        }

                    }

                }


            }
        }

    }

}