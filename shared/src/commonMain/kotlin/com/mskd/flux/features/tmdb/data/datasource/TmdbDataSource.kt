package com.mskd.flux.features.tmdb.data.datasource

import com.mskd.flux.features.tmdb.data.dto.ArtworkDto
import com.mskd.flux.features.tmdb.data.dto.EpisodeDto
import com.mskd.flux.features.tmdb.data.dto.MovieDto
import com.mskd.flux.features.tmdb.data.dto.SeasonDto
import com.mskd.flux.features.tmdb.data.dto.TranslationsDto
import com.mskd.flux.core.domain.model.files.UserFile
import java.util.Locale

interface TmdbDataSource {

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