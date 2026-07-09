package com.mskd.flux.core.database.data.repository

import com.mskd.flux.core.database.data.DatabaseDao
import com.mskd.flux.core.database.data.mappers.toDomain
import com.mskd.flux.core.database.data.mappers.toEntity
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.utils.extensions.sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DatabaseRepositoryImpl(private val dao: DatabaseDao) : DatabaseRepository {

    override fun flowArtworks(): Flow<List<Artwork>> {
        return dao.flowArtworks().map { entities -> entities.map { it.toDomain() } }
    }

    override fun flowArtwork(artworkId: Long): Flow<Artwork?> {
        return dao.flowArtwork(artworkId = artworkId).map { it?.toDomain() }
    }

    override fun flowMovie(artworkId: Long): Flow<Movie?> {
        return dao.flowMovie(artworkId = artworkId).map { it?.toDomain() }
    }

    override fun flowEpisodes(artworkId: Long): Flow<List<Episode>> {
        return dao.flowEpisodes(artworkId = artworkId).map { entities -> entities.map { it.toDomain() }.sort() }
    }

    override fun flowSeasons(artworkId: Long): Flow<List<Season>> {
        return dao.flowSeasons(artworkId = artworkId).map { entities ->  entities.map { it.toDomain() }.sortedBy { s -> s.season } }
    }

    override suspend fun saveArtworks(artworks: List<Artwork>) {
        dao.insertArtworks(artworks = artworks.map { it.toEntity() })
    }

    override suspend fun saveMovies(movies: List<Movie>) {
        dao.insertMovies(movies = movies.map { it.toEntity() })
    }

    override suspend fun saveSeasons(seasons: List<Season>) {
        dao.insertSeasons(seasons.map { it.toEntity() })
    }

    override suspend fun saveEpisodes(episodes: List<Episode>) {
        dao.insertEpisodes(episodes = episodes.map { it.toEntity() })
    }

    override suspend fun getArtwork(artworkId: Long): Artwork? {
        return dao.getArtwork(artworkId = artworkId)?.toDomain()
    }

    override suspend fun getArtworks(): List<Artwork> {
        return dao.getArtworks().map { it.toDomain() }
    }

    override suspend fun getMovie(artworkId: Long): Movie? {
        return dao.getMovie(artworkId = artworkId)?.toDomain()
    }

    override suspend fun getMovies(): List<Movie> {
        return dao.getMovies().map { it.toDomain() }
    }

    override suspend fun getMoviesNotInFiles(files: List<UserFile>): List<Movie> {
        return dao.getMoviesNotInFiles(fileNames =  files.map { it.name }).map { it.toDomain() }
    }

    override suspend fun getEpisodes(artworkId: Long): List<Episode> {
        return dao.getEpisodes(artworkId = artworkId).map { it.toDomain() }.sort()
    }

    override suspend fun getEpisodes(): List<Episode> {
        return dao.getEpisodes().map { it.toDomain() }.sort()
    }

    override suspend fun getEpisodesNotInFiles(files: List<UserFile>): List<Episode> {
        return dao.getEpisodesNotInFiles(fileNames =  files.map { it.name }).map { it.toDomain() }
    }

    override suspend fun getEpisodeCount(artworkId: Long): Int {
        return dao.getEpisodeCountByArtworkId(artworkId = artworkId)
    }

    override suspend fun getEpisodeCountBySeason(artworkId: Long, season: Int): Int {
        return dao.getEpisodeCountBySeason(artworkId = artworkId, season = season)
    }

    override suspend fun getSeasons(artworkId: Long): List<Season> {
        return dao.getSeasons(artworkId).map { it.toDomain() }.sortedBy { it.season }
    }

    override suspend fun getSeasons(): List<Season> {
        return dao.getSeasons().map { it.toDomain() }
    }

    override suspend fun getUnknownMedias(): List<Episode> {
        return dao.getUnknownMedias().map { it.toDomain() }
    }

    override suspend fun getAllImagesPaths(): List<String> {
        val artworks = dao.getArtworksImages()
        val episodes = dao.getEpisodesImages()
        val seasons = dao.getSeasonsImages()

        return buildList {
            addAll(artworks.filter { it.imagePath.isNotBlank() }.map { it.imagePath })
            addAll(artworks.filter { it.bannerPath.isNotBlank() }.map { it.bannerPath })
            addAll(episodes.filter { it.isNotBlank() })
            addAll(seasons.filter { it.isNotBlank() })
        }
    }

    override suspend fun deleteArtworks(artworks: List<Artwork>) {

        val artworkIds = artworks.map { it.id }.distinct()
        dao.deleteArtworks(ids = artworkIds)
        dao.deleteMoviesByIds(ids = artworkIds)
        artworkIds.forEach { dao.deleteEpisodesByArtworkId(artworkId = it) }
        dao.deleteSeasonsByIds(artworkIds = artworkIds)

    }

    override suspend fun deleteMovies(movies: List<Movie>) {

        // Delete movies
        dao.deleteMoviesByIds(movies.map { it.artworkId })

        // Delete related artworks
        dao.deleteArtworks(ids = movies.map { it.artworkId })
    }

    override suspend fun deleteEpisodes(episodes: List<Episode>) {

        // Delete episodes
        dao.deleteEpisodesByIds(episodes.map { it.id })

        // Delete empty seasons and artworks
        dao.deleteEmptySeasons()
        dao.deleteEmptyArtworks()

    }

    override suspend fun deleteMediasNotInFiles(files: List<UserFile>) {

        val moviesToDelete = getMoviesNotInFiles(files = files)
        val episodesToDelete = getEpisodesNotInFiles(files = files)

        deleteMovies(moviesToDelete)
        deleteEpisodes(episodesToDelete)

    }

    override suspend fun deleteMediasInFolder(folder: UserFolder) {

        dao.deleteMediasInFolder(folderPath = folder.path)

        dao.deleteEmptySeasons()
        dao.deleteEmptyArtworks()

    }

    override suspend fun deleteAll() {
        dao.deleteAllArtworks()
        dao.deleteAllMovies()
        dao.deleteAllEpisodes()
        dao.deleteAllSeasons()
    }
}