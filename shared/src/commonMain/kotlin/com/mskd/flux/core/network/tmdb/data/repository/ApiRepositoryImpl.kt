package com.mskd.flux.core.network.tmdb.data.repository

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.core.network.tmdb.data.dto.images.ImagesDto
import com.mskd.flux.core.network.tmdb.data.mapper.logoPath
import com.mskd.flux.core.network.tmdb.data.mapper.toArtwork
import com.mskd.flux.core.network.tmdb.data.mapper.toCandidate
import com.mskd.flux.core.network.tmdb.data.mapper.toDomain
import com.mskd.flux.core.network.tmdb.domain.model.ArtworkCandidate
import com.mskd.flux.core.network.tmdb.domain.model.ArtworkDetails
import com.mskd.flux.core.network.tmdb.domain.model.PersonInfo
import com.mskd.flux.core.network.tmdb.domain.model.Translation
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore

internal class ApiRepositoryImpl(
    private val tmdb: TmdbDataSource,
    private val settings: SettingsDataStore,
) : ApiRepository {

    //region Artwork

    override suspend fun getArtwork(file: UserFile): Artwork = tmdb.getArtwork(file = file)?.toDomain() ?: Artwork.UNKNOWN

    override suspend fun getArtworkById(id: Long, type: ContentType): Artwork? = when (type) {
        ContentType.MOVIE -> tmdb.getMovie(artworkId = id)?.toArtwork()
        ContentType.SHOW -> tmdb.getShow(artworkId = id)?.toArtwork()
    }

    override suspend fun searchArtworks(title: String, type: ContentType): List<ArtworkCandidate> =
        tmdb.searchArtworks(title = title, type = type).map { it.toCandidate() }

    override suspend fun getDetails(artworkId: Long, type: ContentType, file: UserFile): ArtworkDetails? = when (type) {
        ContentType.MOVIE -> tmdb.getMovie(artworkId = artworkId)?.let { movie ->
            ArtworkDetails(
                artwork = movie.toArtwork(logoPath = movie.images.logoPath()),
                movie = movie.toDomain(file = file, duration = movie.duration ?: 0),
                people = movie.credits?.toDomain(artworkId = movie.id).orEmpty()
            )
        }
        ContentType.SHOW -> tmdb.getShow(artworkId = artworkId)?.let { show ->
            ArtworkDetails(
                artwork = show.toArtwork(logoPath = show.images.logoPath()),
                movie = null,
                people = show.credits?.toDomain(artworkId = show.id).orEmpty()
            )
        }
    }

    /** Logo among the images appended to the details; null (not searched) when the details came without images. */
    private suspend fun ImagesDto?.logoPath(): String? = this?.logoPath(language = settings.getDataLanguage())

    override suspend fun getLogo(artworkId: Long, type: ContentType): String {
        val images = when (type) {
            ContentType.MOVIE -> tmdb.getMovieImages(artworkId = artworkId)
            ContentType.SHOW -> tmdb.getShowImages(artworkId = artworkId)
        }
        return images.logoPath(language = settings.getDataLanguage())
    }

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

    override suspend fun getMoviePeople(artworkId: Long): List<Person> {
        return tmdb.getMoviePeople(artworkId = artworkId)?.toDomain(artworkId = artworkId) ?: emptyList()
    }

    override suspend fun getMovieImages(artworkId: Long): List<GalleryImage> =
        tmdb.getMovieImages(artworkId = artworkId).toDomain()

    //endregion

    //region Show

    override suspend fun getShowImages(artworkId: Long): List<GalleryImage> =
        tmdb.getShowImages(artworkId = artworkId).toDomain()

    override suspend fun getSeasonAndEpisodes(
        artworkId: Long,
        season: Int,
        files: List<UserFile>
    ): Pair<Season, List<Episode>>? {

        val seasonDto = tmdb.getSeason(artworkId = artworkId, season = season) ?: return  null

        val season = seasonDto.toDomain(artworkId = artworkId)

        // Several files can resolve to the same episode (same file from several sources, duplicated
        // file, same episode in different qualities...) and the primary key of medias is (id, artworkId):
        // keep only one media per episode.
        val episodes = files
            .distinctBy { it.season to it.episode }
            .map { file ->
                seasonDto.episodes
                    .find { it.season == file.season && it.number == file.episode }
                    ?.toDomain(artworkId = artworkId, file = file)
                    ?: Episode(file = file)
            }

        return season to episodes
    }

    override suspend fun getShowPeople(artworkId: Long): List<Person> {
        return tmdb.getShowPeople(artworkId = artworkId)?.toDomain(artworkId = artworkId) ?: emptyList()
    }

    override suspend fun getPersonInfo(personId: Int): PersonInfo? {
        return tmdb.getPersonInfo(personId = personId)?.toDomain()
    }

    //endregion

    //region Global

    override suspend fun getTranslation(request: TranslationRequest): Translation? {
        return tmdb.getTranslation(request = request)?.let {
                Translation(
                    title = it.data.text,
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