package com.mskd.flux.core.database.data.repository

import com.mskd.flux.core.database.data.dao.ArtworkDao
import com.mskd.flux.core.database.data.dao.MediasDao
import com.mskd.flux.core.database.data.dao.SeasonsDao
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

class DatabaseRepositoryImpl(
    private val artworksDao: ArtworkDao,
    private val mediasDao: MediasDao,
    private val seasonsDao: SeasonsDao
) : DatabaseRepository {

    override fun flowArtworks(): Flow<List<Artwork>> {
        return artworksDao.flowArtworks().map { entities -> entities.map { it.toDomain() } }
    }

    override fun flowArtwork(artworkId: Long): Flow<Artwork?> {
        return artworksDao.flowArtwork(artworkId = artworkId).map { it?.toDomain() }
    }

    override fun flowMedias(artworkId: Long): Flow<List<Media>> {
        return mediasDao.flow(artworkId = artworkId).map { entities -> entities.map { it.toDomain() } }
    }

    override fun flowSeasons(artworkId: Long): Flow<List<Season>> {
        return seasonsDao.flow(artworkId = artworkId).map { entities ->  entities.map { it.toDomain() }.sortedBy { s -> s.season } }
    }

    override suspend fun saveArtworks(artworks: List<Artwork>, overrideLastModification: Boolean) {
        artworksDao.insertArtworks(artworks = artworks.map { it.toEntity(overrideLastModification = overrideLastModification) })
    }

    override suspend fun saveMedias(medias: List<Media>) {
        mediasDao.insertOrUpdate(medias = medias.map { it.toEntity() })
    }

    override suspend fun saveSeasons(seasons: List<Season>) {
        seasonsDao.insert(seasons.map { it.toEntity() })
    }

    override suspend fun getArtwork(artworkId: Long): Artwork? {
        return artworksDao.getArtwork(artworkId = artworkId)?.toDomain()
    }

    override suspend fun getArtworks(): List<Artwork> {
        return artworksDao.getArtworks().map { it.toDomain() }
    }

    override suspend fun getMedias(): List<Media> {
        return mediasDao.getAll().map { it.toDomain() }
    }

    override suspend fun getMediasNotInFiles(files: List<UserFile>): List<Media> {
        return mediasDao.getMediasNotInFiles(fileNames =  files.map { it.name }).map { it.toDomain() }
    }

    override suspend fun getMovie(artworkId: Long): Movie? {
        return mediasDao.getForArtwork(artworkId = artworkId).find { it.type == ContentType.MOVIE }?.toDomain() as? Movie
    }

    override suspend fun getEpisodes(artworkId: Long): List<Episode> {
        return mediasDao.getForArtwork(artworkId = artworkId).mapNotNull { it.toDomain() as? Episode }.sort()
    }

    override suspend fun getEpisodeCount(artworkId: Long): Int {
        return mediasDao.getEpisodeCountByArtworkId(artworkId = artworkId)
    }

    override suspend fun getEpisodeCountBySeason(artworkId: Long, season: Int): Int {
        return mediasDao.getEpisodeCountBySeason(artworkId = artworkId, season = season)
    }

    override suspend fun getSeasons(artworkId: Long): List<Season> {
        return seasonsDao.getForArtwork(artworkId).map { it.toDomain() }.sortedBy { it.season }
    }

    override suspend fun getSeasons(): List<Season> {
        return seasonsDao.getAll().map { it.toDomain() }
    }

    override suspend fun getUnknownMedias(): List<Episode> {
        return mediasDao.getUnknownMedias().mapNotNull { it.toDomain() as? Episode }
    }

    override suspend fun getAllImagesPaths(): List<String> {
        val artworks = artworksDao.getArtworksImages()
        val medias = mediasDao.getMediasImages()
        val seasons = seasonsDao.getImages()

        return buildList {
            addAll(artworks.filter { it.imagePath.isNotBlank() }.map { it.imagePath })
            addAll(artworks.filter { it.bannerPath.isNotBlank() }.map { it.bannerPath })
            addAll(medias.filter { it.isNotBlank() })
            addAll(seasons.filter { it.isNotBlank() })
        }
    }

    override suspend fun updateRealPaths(files: List<UserFile>) {
        mediasDao.updateRealPaths(files = files)
    }

    override suspend fun deleteArtworks(artworks: List<Artwork>) {
        val artworkIds = artworks.map { it.id }.distinct()

        artworksDao.deleteArtworks(artworkIds = artworkIds)
        mediasDao.deleteMediasByArtworkIds(artworkIds = artworkIds)
        seasonsDao.deleteByArtworkIds(artworkIds = artworkIds)
    }

    suspend fun deleteMedias(medias: List<Media>) {

        // Delete movies
        medias.filterIsInstance<Movie>().let { movies ->
            mediasDao.deleteMediasByArtworkIds(movies.map { it.artworkId })
        }

        // Delete episodes
        medias.filterIsInstance<Episode>().let { episodes ->
            mediasDao.deleteEpisodesByIds(episodes.map { it.id })

        }

        seasonsDao.deleteEmptySeasons() // Delete empty seasons
        artworksDao.deleteEmptyArtworks() // Clean empty artworks
    }


    override suspend fun deleteMediasNotInFiles(files: List<UserFile>) {

        val mediasToDelete = getMediasNotInFiles(files = files)
        deleteMedias(medias = mediasToDelete)
    }

    override suspend fun deleteMediasInFolder(folder: UserFolder) {

        mediasDao.deleteMediasInFolder(folderPath = folder.path)

        seasonsDao.deleteEmptySeasons()
        artworksDao.deleteEmptyArtworks()

    }

    override suspend fun deleteAll() {
        artworksDao.deleteAllArtworks()
        mediasDao.deleteAllMedias()
        seasonsDao.deleteAll()
    }
}