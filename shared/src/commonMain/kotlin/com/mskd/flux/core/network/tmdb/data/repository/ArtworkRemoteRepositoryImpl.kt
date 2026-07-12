package com.mskd.flux.core.network.tmdb.data.repository

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.core.network.tmdb.data.dto.EpisodeDto
import com.mskd.flux.core.network.tmdb.data.mapper.toDomain
import com.mskd.flux.core.network.tmdb.domain.model.Translation
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.core.network.tmdb.domain.repository.ArtworkRemoteRepository
import java.util.Locale

internal class ArtworkRemoteRepositoryImpl(
    private val tmdb: TmdbDataSource
) : ArtworkRemoteRepository {

    override suspend fun getArtwork(file: UserFile): Artwork? =
        tmdb.getTmdbArtwork(file = file)?.toDomain()

    override suspend fun getMovie(
        artworkId: Long,
        file: UserFile,
        fallbackDuration: suspend () -> Int
    ): Media? {
        val tmdbMovie = tmdb.getTmdbMovie(artworkId = artworkId) ?: return null
        return tmdbMovie.toDomain(
            file = file,
            duration = tmdbMovie.duration ?: fallbackDuration()
        )
    }

    override suspend fun getSeason(artworkId: Long, season: Int): Pair<Season, List<EpisodeDto>>? =
        tmdb.getTmdbSeason(artworkId = artworkId, season = season)?.let {
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
            tmdb.translateTmdbEpisode(artworkId = artworkId, episodeDto = episodeDto, language = language)
        } else {
            episodeDto
        }

        return resolvedDto.toDomain(
            artworkId = artworkId,
            file = file,
            duration = resolvedDto.duration ?: fallbackDuration()
        )

    }

    override suspend fun translate(request: TranslationRequest): Translation? {
        return tmdb.getTmdbTranslation(request = request)
            ?.let {
                Translation(
                    title = it.data.name,
                    description = it.data.overview
                )
            }
    }

}