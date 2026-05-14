package com.mskd.flux.data.repository.ddb

import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.utils.extensions.tmdbImage
import com.mskd.flux.utils.extensions.tmdbImageLarge
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DatabaseRepositoryImpl @Inject constructor(private val dao: DatabaseDao) : DatabaseRepository {

    override fun flowArtworks(): Flow<List<Artwork>> {
        return dao.flowArtworks()
    }

    override fun flowArtwork(artworkId: Long): Flow<Artwork?> {
        return dao.flowArtwork(artworkId = artworkId)
    }

    override fun flowMovie(artworkId: Long): Flow<Movie?> {
        return dao.flowMovie(artworkId = artworkId)
    }

    override fun flowEpisodes(artworkId: Long): Flow<List<Episode>> {
        return dao.flowEpisodes(artworkId = artworkId)
    }

    override suspend fun saveArtworks(artworks: List<Artwork>) {
        dao.insertArtworks(artworks = artworks)
    }

    override suspend fun saveMovies(movies: List<Movie>) {
        dao.insertMovies(movies = movies)
    }

    override suspend fun saveEpisodes(episodes: List<Episode>) {
        dao.insertEpisodes(episodes = episodes)
    }

    override suspend fun getArtwork(artworkId: Long): Artwork? {
        return dao.getArtwork(artworkId = artworkId)
    }

    override suspend fun getArtworks(): List<Artwork> {
        return dao.getArtworks()
    }

    override suspend fun getMovie(artworkId: Long): Movie? {
        return dao.getMovie(artworkId = artworkId)
    }

    override suspend fun getMovies(): List<Movie> {
        return dao.getMovies()
    }

    override suspend fun getMoviesNotInFiles(files: List<UserFile>): List<Movie> {
        return dao.getMoviesNotInFiles(fileNames =  files.map { it.name })
    }

    override suspend fun getEpisode(episodeId: Long): Episode? {
        return dao.getEpisode(episodeId = episodeId)
    }

    override suspend fun getEpisodes(artworkId: Long): List<Episode> {
        return dao.getEpisodes(artworkId = artworkId)
    }

    override suspend fun getEpisodes(): List<Episode> {
        return dao.getEpisodes()
    }

    override suspend fun getEpisodesNotInFiles(files: List<UserFile>): List<Episode> {
        return dao.getEpisodesNotInFiles(fileNames =  files.map { it.name })
    }

    override suspend fun getEpisodeCount(artworkId: Long): Int {
        return dao.getEpisodeCountByArtworkId(artworkId = artworkId)
    }

    override suspend fun getUnknownMedias(): List<Episode> {
        return dao.getUnknownMedias()
    }

    override suspend fun getAllImagesPaths(): List<String> {
        val artworks = dao.getArtworksImages()
        val episodes = dao.getEpisodesImages()

        return buildList {
            addAll(artworks.map { it.imagePath.tmdbImage })
            addAll(artworks.map { it.bannerPath.tmdbImageLarge })
            addAll(episodes.map { it.tmdbImage })
        }
    }

    override suspend fun deleteArtworks(artworks: List<Artwork>) {
        dao.deleteArtworks(ids = artworks.map { it.id })
    }

    override suspend fun deleteMovies(movies: List<Movie>) {
        dao.deleteArtworks(ids = movies.map { it.artworkId })
    }

    override suspend fun deleteEpisodes(episodes: List<Episode>) {

        // Delete episodes
        dao.deleteEpisodesByIds(episodes.map { it.id })

        // Delete artworks if needed
        episodes
            .map { it.artworkId }
            .distinct()
            .forEach { artworkId ->

                // Check if it remains episode for show
                val remainingEpisodes = getEpisodeCount(artworkId = artworkId)

                // If no, delete the show
                if (remainingEpisodes == 0) {
                    dao.deleteArtworks(ids = listOf(artworkId))
                }

            }

    }

    override suspend fun deleteMediasNotInFiles(files: List<UserFile>) {

        val moviesToDelete = getMoviesNotInFiles(files = files)
        val episodesToDelete = getEpisodesNotInFiles(files = files)

        deleteMovies(moviesToDelete)
        deleteEpisodes(episodesToDelete)

    }

    override suspend fun deleteAll() {
        dao.deleteAllArtworks()
        dao.deleteAllMovies()
        dao.deleteAllEpisodes()
    }
}