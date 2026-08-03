package com.mskd.flux.core.database.data.repository

import com.mskd.flux.core.database.data.DatabaseDao
import com.mskd.flux.core.database.data.mappers.toDomain
import com.mskd.flux.core.database.data.mappers.toEntity
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
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

    override fun flowMedias(artworkId: Long): Flow<List<Media>> {
        return dao.flowMedias(artworkId = artworkId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun flowSeasons(artworkId: Long): Flow<List<Season>> {
        return dao.flowSeasons(artworkId = artworkId).map { entities ->  entities.map { it.toDomain() }.sortedBy { s -> s.season } }
    }

    override suspend fun saveArtworks(artworks: List<Artwork>) {
        dao.insertArtworks(artworks = artworks.map { it.toEntity() })
    }

    override suspend fun saveMedias(medias: List<Media>) {
        dao.insertMedias(medias = medias.map { it.toEntity() })
    }

    override suspend fun saveSeasons(seasons: List<Season>) {
        dao.insertSeasons(seasons.map { it.toEntity() })
    }

    override suspend fun getArtwork(artworkId: Long): Artwork? {
        return dao.getArtwork(artworkId = artworkId)?.toDomain()
    }

    override suspend fun getArtworks(): List<Artwork> {
        return dao.getArtworks().map { it.toDomain() }
    }

    override suspend fun getMedias(): List<Media> {
        return dao.getMedias().map { it.toDomain() }
    }

    override suspend fun getMediasNotInFiles(files: List<UserFile>): List<Media> {
        return dao.getMediasNotInFiles(fileNames =  files.map { it.name }).map { it.toDomain() }
    }

    override suspend fun getMovie(artworkId: Long): Movie? {
        return dao.getMedias(artworkId = artworkId).find { it.type == ContentType.MOVIE }?.toDomain() as? Movie
    }

    override suspend fun getEpisodes(artworkId: Long): List<Episode> {
        return dao.getMedias(artworkId = artworkId).mapNotNull { it.toDomain() as? Episode }.sort()
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
        return dao.getUnknownMedias().mapNotNull { it.toDomain() as? Episode }
    }

    override suspend fun getAllImagesPaths(): List<String> {
        val artworks = dao.getArtworksImages()
        val medias = dao.getMediasImages()
        val seasons = dao.getSeasonsImages()

        return buildList {
            addAll(artworks.filter { it.imagePath.isNotBlank() }.map { it.imagePath })
            addAll(artworks.filter { it.bannerPath.isNotBlank() }.map { it.bannerPath })
            addAll(medias.filter { it.isNotBlank() })
            addAll(seasons.filter { it.isNotBlank() })
        }
    }

    override suspend fun updateRealPaths(files: List<UserFile>) {
        dao.updateRealPaths(files = files)
    }

    override suspend fun deleteArtworks(artworks: List<Artwork>) {
        val artworkIds = artworks.map { it.id }.distinct()

        dao.deleteArtworks(artworkIds = artworkIds)
        dao.deleteMediasByArtworkIds(artworkIds = artworkIds)
        dao.deleteSeasonsByArtworkIds(artworkIds = artworkIds)
    }

    suspend fun deleteMedias(medias: List<Media>) {

        // Delete movies
        medias.filterIsInstance<Movie>().let { movies ->
            dao.deleteMediasByArtworkIds(movies.map { it.artworkId })
        }

        // Delete episodes
        medias.filterIsInstance<Episode>().let { episodes ->
            dao.deleteEpisodesByIds(episodes.map { it.id })

        }

        dao.deleteEmptySeasons() // Delete empty seasons
        dao.deleteEmptyArtworks() // Clean empty artworks
    }


    override suspend fun deleteMediasNotInFiles(files: List<UserFile>) {

        val mediasToDelete = getMediasNotInFiles(files = files)
        deleteMedias(medias = mediasToDelete)
    }

    override suspend fun deleteMediasInFolder(folder: UserFolder) {

        dao.deleteMediasInFolder(folderPath = folder.path)

        dao.deleteEmptySeasons()
        dao.deleteEmptyArtworks()

    }

    override suspend fun deleteAll() {
        dao.deleteAllArtworks()
        dao.deleteAllMedias()
        dao.deleteAllSeasons()
    }
}