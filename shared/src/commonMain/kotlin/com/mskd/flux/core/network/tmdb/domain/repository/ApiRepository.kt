package com.mskd.flux.core.network.tmdb.domain.repository

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.dto.show.EpisodeDto
import com.mskd.flux.core.network.tmdb.domain.model.Translation
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import java.util.Locale

interface ApiRepository {

    suspend fun getArtwork(file: UserFile): Artwork

    suspend fun getGenres() : List<Genre>

    suspend fun getMovie(artworkId: Long, file: UserFile): Media?

    suspend fun getSeason(artworkId: Long, season: Int): Pair<Season, List<EpisodeDto>>?
    suspend fun getSeasonAndEpisodes(artworkId: Long, season: Int, files: List<UserFile>): Pair<Season, List<Episode>>?

    suspend fun resolveEpisode(
        artworkId: Long,
        episodeDto: EpisodeDto,
        file: UserFile,
        language: Locale,
        fallbackDuration: suspend () -> Int
    ): Episode

    suspend fun translate(request: TranslationRequest): Translation?

}