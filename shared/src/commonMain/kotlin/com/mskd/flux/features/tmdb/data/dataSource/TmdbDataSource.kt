package com.mskd.flux.features.tmdb.data.dataSource

import com.mskd.flux.features.tmdb.data.model.dto.ArtworkDto
import com.mskd.flux.features.tmdb.data.model.dto.EpisodeDto
import com.mskd.flux.features.tmdb.data.model.dto.MovieDto
import com.mskd.flux.features.tmdb.data.model.dto.SeasonDto
import com.mskd.flux.features.tmdb.data.model.dto.TranslationsDto
import com.mskd.flux.model.domain.files.UserFile
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