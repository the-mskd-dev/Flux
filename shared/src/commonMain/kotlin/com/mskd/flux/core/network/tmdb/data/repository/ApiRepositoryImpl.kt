package com.mskd.flux.core.network.tmdb.data.repository

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.core.network.tmdb.data.mapper.toDomain
import com.mskd.flux.core.network.tmdb.domain.model.Translation
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository

internal class ApiRepositoryImpl(
    private val tmdb: TmdbDataSource
) : ApiRepository {

    //region Artwork

    override suspend fun getArtwork(file: UserFile): Artwork = tmdb.getArtwork(file = file)?.toDomain() ?: Artwork.UNKNOWN

    override suspend fun getGenres(): List<Genre> = tmdb.getGenres().map { it.toDomain() }

    //endregion

    //region Movie

    override suspend fun getMovie(
        artworkId: Long,
        file: UserFile,
    ): Movie? {
        val tmdbMovie = tmdb.getMovie(artworkId = artworkId) ?: return null
        return tmdbMovie.toDomain(
            file = file,
            duration = tmdbMovie.duration ?: 0
        )
    }

    //endregion

    //region Show

    override suspend fun getSeasonAndEpisodes(
        artworkId: Long,
        season: Int,
        files: List<UserFile>
    ): Pair<Season, List<Episode>>? {

        val seasonDto = tmdb.getSeason(artworkId = artworkId, season = season) ?: return  null

        val season = seasonDto.toDomain(artworkId = artworkId)

        val episodes = files.map { file ->
            seasonDto.episodes
                .find { it.season == file.season && it.number == file.episode }
                ?.toDomain(artworkId = artworkId, file = file)
                ?: Episode(file = file)
        }

        return season to episodes
    }

    //endregion

    //region Global

    override suspend fun getTranslation(request: TranslationRequest): Translation? {
        return tmdb.getTranslation(request = request)
            ?.let {
                Translation(
                    title = it.data.name,
                    description = it.data.overview
                )
            }
    }

    override suspend fun getGenreIds(artwork: Artwork): List<Int> {
        return when (artwork.type) {
            ContentType.MOVIE -> tmdb.getMovie(artworkId = artwork.id)?.genres
            ContentType.SHOW -> tmdb.getShow(artworkId = artwork.id)?.genres
        }?.map { it.id } ?: emptyList()
    }

    //endregion

}