package com.mskd.flux.core.network.tmdb.data.datasource

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.dto.ArtworkDto
import com.mskd.flux.core.network.tmdb.data.dto.TranslationsDto
import com.mskd.flux.core.network.tmdb.data.dto.genre.GenreDto
import com.mskd.flux.core.network.tmdb.data.dto.movie.MovieDto
import com.mskd.flux.core.network.tmdb.data.dto.show.EpisodeDto
import com.mskd.flux.core.network.tmdb.data.dto.show.SeasonDto
import com.mskd.flux.core.network.tmdb.data.dto.show.ShowDto
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest

interface TmdbDataSource {

    suspend fun getArtwork(file: UserFile) : ArtworkDto?

    suspend fun getGenres() : List<GenreDto>

    suspend fun getMovie(artworkId: Long) : MovieDto?

    suspend fun getShow(artworkId: Long) : ShowDto?

    suspend fun getEpisode(
        artworkId: Long,
        season: Int,
        number: Int,
    ) : EpisodeDto?

    suspend fun getSeason(
        artworkId: Long,
        season: Int,
    ) : SeasonDto?

    suspend fun getTranslation(request: TranslationRequest) : TranslationsDto.Translation?

}