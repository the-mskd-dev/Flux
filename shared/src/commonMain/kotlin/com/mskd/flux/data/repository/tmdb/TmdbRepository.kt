package com.mskd.flux.data.repository.tmdb

import com.mskd.flux.model.data.remote.tmdb.dto.ArtworkDto
import com.mskd.flux.model.data.remote.tmdb.dto.EpisodeDto
import com.mskd.flux.model.data.remote.tmdb.dto.MovieDto
import com.mskd.flux.model.data.remote.tmdb.dto.SeasonDto
import com.mskd.flux.model.data.remote.tmdb.dto.TranslationsDto
import com.mskd.flux.model.domain.UserFile
import java.util.Locale

interface TmdbRepository {

    suspend fun getTmdbArtwork(file: UserFile) : ArtworkDto?

    suspend fun getTmdbMovie(artworkId: Long) : MovieDto?

    suspend fun getTmdbEpisode(
        artworkId: Long,
        season: Int,
        number: Int,
    ) : EpisodeDto?

    suspend fun getTmdbSeason(
        artworkId: Long,
        season: Int,
    ) : SeasonDto?

    suspend fun translateTmdbEpisode(artworkId: Long, episodeDto: EpisodeDto, language: Locale) : EpisodeDto

    suspend fun getTmdbTranslation(request: TranslationsDto.Request) : TranslationsDto.Translation?

}