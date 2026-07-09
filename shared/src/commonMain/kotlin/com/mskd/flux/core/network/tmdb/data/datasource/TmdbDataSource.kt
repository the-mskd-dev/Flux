package com.mskd.flux.core.network.tmdb.data.datasource

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.remote.dto.ArtworkDto
import com.mskd.flux.core.network.tmdb.data.remote.dto.EpisodeDto
import com.mskd.flux.core.network.tmdb.data.remote.dto.MovieDto
import com.mskd.flux.core.network.tmdb.data.remote.dto.SeasonDto
import com.mskd.flux.core.network.tmdb.data.remote.dto.TranslationsDto
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