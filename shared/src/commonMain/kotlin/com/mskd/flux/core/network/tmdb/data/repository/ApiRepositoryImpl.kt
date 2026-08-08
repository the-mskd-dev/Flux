package com.mskd.flux.core.network.tmdb.data.repository

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.core.network.tmdb.data.dto.show.EpisodeDto
import com.mskd.flux.core.network.tmdb.data.mapper.toDomain
import com.mskd.flux.core.network.tmdb.domain.model.Translation
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import java.util.Locale

internal class ApiRepositoryImpl(
    private val tmdb: TmdbDataSource
) : ApiRepository {

    //region Artwork

    override suspend fun getArtwork(file: UserFile): Artwork? =
        tmdb.getArtwork(file = file)?.toDomain()

    override suspend fun getGenres(): List<Genre> =
        tmdb.getGenres().map { it.toDomain() }

    //endregion

    //region Movie

    override suspend fun getMovie(
        artworkId: Long,
        file: UserFile,
        fallbackDuration: suspend () -> Int
    ): Media? {
        val tmdbMovie = tmdb.getMovie(artworkId = artworkId) ?: return null
        return tmdbMovie.toDomain(
            file = file,
            duration = tmdbMovie.duration ?: fallbackDuration()
        )
    }

    //endregion

    //region Show

    override suspend fun getSeason(artworkId: Long, season: Int): Pair<Season, List<EpisodeDto>>? =
        tmdb.getSeason(artworkId = artworkId, season = season)?.let {
            it.toDomain(artworkId = artworkId) to it.episodes
        }

    override suspend fun resolveEpisode(
        artworkId: Long,
        episodeDto: EpisodeDto,
        file: UserFile,
        language: Locale,
        fallbackDuration: suspend () -> Int
    ): Episode {

        val resolvedDto = if (episodeDto.title.isBlank() || episodeDto.description.isBlank()) {
            tmdb.translateEpisode(artworkId = artworkId, episodeDto = episodeDto, language = language)
        } else {
            episodeDto
        }

        return resolvedDto.toDomain(
            artworkId = artworkId,
            file = file,
            duration = resolvedDto.duration ?: fallbackDuration()
        )

    }

    //endregion

    //region Global

    override suspend fun translate(request: TranslationRequest): Translation? {
        return tmdb.getTranslation(request = request)
            ?.let {
                Translation(
                    title = it.data.name,
                    description = it.data.overview
                )
            }
    }

    //endregion

}