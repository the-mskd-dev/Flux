package com.mskd.flux.core.network.tmdb.domain.repository

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.dto.EpisodeDto
import com.mskd.flux.core.network.tmdb.domain.model.Translation
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import java.util.Locale

interface ArtworkRemoteRepository {

    suspend fun getArtwork(file: UserFile): Artwork?

    suspend fun getMovie(artworkId: Long, file: UserFile, fallbackDuration: suspend () -> Int): Media?

    suspend fun getSeason(artworkId: Long, season: Int): Pair<Season, List<EpisodeDto>>?

    suspend fun resolveEpisode(
        artworkId: Long,
        episodeDto: EpisodeDto,
        file: UserFile,
        language: Locale,
        fallbackDuration: suspend () -> Int
    ): Episode

    suspend fun translate(request: TranslationRequest): Translation?

}