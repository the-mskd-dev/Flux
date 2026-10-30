package com.mskd.flux.core.database.data.repository

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.mskd.flux.core.database.data.FluxDatabase
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
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.core.ImagesPaths
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.history.data.model.HistoryEntity
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.utils.extensions.sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.mskd.flux.features.people.data.mapper.toEntity as toPersonEntity

class DatabaseRepositoryImpl(
    private val database: FluxDatabase,
) : DatabaseRepository {

    private val artworksDao: ArtworkDao = database.artworkDao()
    private val mediasDao: MediasDao = database.mediasDao()
    private val seasonsDao: SeasonsDao = database.seasonsDao()

    override fun flowArtworks(includePrivates: Boolean): Flow<List<Artwork>> {
        return artworksDao.flowArtworks(includePrivates = includePrivates).map { entities -> entities.map { it.toDomain() } }
    }

    override fun flowPrivateArtworks(): Flow<List<Artwork>> {
        return artworksDao.flowPrivateArtworks().map { entities -> entities.map { it.toDomain() } }
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

    override suspend fun saveMedias(medias: List<Media>) {
        mediasDao.insertOrUpdate(medias = medias.map { it.toEntity() })
    }

    override suspend fun saveSyncedMedias(medias: List<Media>) {
        mediasDao.upsertFromSync(medias = medias.map { it.toEntity() })
    }

    override suspend fun saveSeasons(seasons: List<Season>) {
        seasonsDao.insert(seasons.map { it.toEntity() })
    }

    override suspend fun saveSyncedFolder(artwork: Artwork, seasons: List<Season>, medias: List<Media>, people: List<Person>) {
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction { saveFolder(artwork = artwork, seasons = seasons, medias = medias, people = people) }
        }
    }

    override suspend fun saveIdentifiedFolder(
        sourceArtworkId: Long,
        artwork: Artwork,
        seasons: List<Season>,
        medias: List<Media>,
        people: List<Person>,
    ) {
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {

                val historyDao = database.historyDao()

                // Read before the save: the history entry is deleted with its Média by the cascade of its foreign key
                val sourceHistory = historyDao.get(artworkId = sourceArtworkId)
                val historyPath = sourceHistory?.let { entry ->
                    mediasDao.findByArtworkIds(artworkIds = listOf(sourceArtworkId)).find { it.id == entry.mediaId }?.path
                }

                // The episodes the chosen Show does not have are Fichiers non reconnus: the Unknown Artwork must exist to show them
                if (medias.any { it.artworkId == Artwork.UNKNOWN_ID } && artworksDao.getArtwork(artworkId = Artwork.UNKNOWN_ID) == null) {
                    saveArtworks(listOf(Artwork.UNKNOWN))
                }

                saveFolder(artwork = artwork, seasons = seasons, medias = medias, people = people)

                if (sourceHistory != null && historyPath != null) {
                    val media = mediasDao.findByPaths(paths = listOf(historyPath)).find { it.artworkId == artwork.id }
                    val targetHistory = historyDao.get(artworkId = artwork.id)
                    if (media != null && (targetHistory == null || targetHistory.timestamp < sourceHistory.timestamp)) {
                        historyDao.upsert(entity = HistoryEntity(artworkId = artwork.id, mediaId = media.id, timestamp = sourceHistory.timestamp))
                    }
                }

                if (artworksDao.getArtwork(artworkId = sourceArtworkId) == null) {
                    database.personDao().delete(artworkId = sourceArtworkId)
                }

            }
        }
    }

    /** Must run in a transaction. */
    private suspend fun saveFolder(artwork: Artwork, seasons: List<Season>, medias: List<Media>, people: List<Person>) {
        // A renamed file keeps its path (MediaStore) but may now be another media: its previous one is replaced,
        // and is no Fichier manquant, so the Artwork and Season it leaves empty must be cleaned here
        val previousByPath = mediasDao.findByPaths(paths = medias.map { it.file.path }).associateBy { it.path }
        val replacesMedias = medias.map { it.toEntity() }.any { media ->
            val previous = previousByPath[media.path] ?: return@any false
            previous.id != media.id || previous.artworkId != media.artworkId
        }
        saveArtworks(listOf(artwork))
        saveSeasons(seasons)
        saveSyncedMedias(medias)
        if (replacesMedias) {
            seasonsDao.deleteEmptySeasons()
            artworksDao.deleteEmptyArtworks()
        }
        if (people.isNotEmpty()) {
            // TMDB returns the whole credits: the ones it dropped must not stay
            database.personDao().apply {
                delete(artworkId = artwork.id)
                insert(people = people.map { it.toPersonEntity() })
            }
        }
    }

    override suspend fun getArtwork(artworkId: Long): Artwork? {
        return artworksDao.getArtwork(artworkId = artworkId)?.toDomain()
    }

    override suspend fun getArtworks(includePrivates: Boolean): List<Artwork> {
        return artworksDao.getArtworks(includePrivates = includePrivates).map { it.toDomain() }
    }

    override suspend fun getArtworks(artworkIds: List<Long>): List<Artwork> {
        return artworksDao.getArtworks(artworkIds = artworkIds).map { it.toDomain() }
    }

    override suspend fun setArtworkLogo(artworkId: Long, logoPath: String?) {
        artworksDao.setArtworkLogo(artworkId = artworkId, logoPath = logoPath)
    }

    override suspend fun setArtworkPrivate(artworkId: Long, isPrivate: Boolean) {
        artworksDao.setArtworkPrivate(artworkId = artworkId, isPrivate = isPrivate)
    }

    override suspend fun clearPrivateArtworks() {
        artworksDao.clearPrivateArtworks()
    }

    override suspend fun getPrivateArtworkIds(): List<Long> {
        return artworksDao.getPrivateArtworkIds()
    }

    override suspend fun setNsfwArtworksPrivate() {
        artworksDao.setNsfwArtworksPrivate()
    }

    override suspend fun getNsfwArtworkIds(): List<Long> {
        return artworksDao.getNsfwArtworkIds()
    }

    override suspend fun saveArtworks(artworks: List<Artwork>, overrideLastModification: Boolean) {
        val privateIds = artworksDao.getPrivateArtworkIds().toSet()
        val manuallyIdentifiedIds = artworksDao.getManuallyIdentifiedArtworkIds().toSet()

        artworksDao.insertArtworks(artworks = artworks.map { artwork ->
            artwork.toEntity(overrideLastModification = overrideLastModification).copy(
                isPrivate = artwork.isPrivate || artwork.id in privateIds,
                // A save never undoes an Identification manuelle: only an explicit clear of the marker does
                manuallyIdentified = artwork.isManuallyIdentified || artwork.id in manuallyIdentifiedIds
            )
        })
    }

    override suspend fun getMedias(): List<Media> {
        return mediasDao.getAll().map { it.toDomain() }
    }

    override suspend fun getMediasNotInFiles(files: List<UserFile>): List<Media> {
        return mediasDao.getMediasNotInFiles(fileNames =  files.map { it.name }).map { it.toDomain() }
    }

    override suspend fun getManuallyIdentifiedMedias(): List<Media> {
        return mediasDao.findByArtworkIds(artworkIds = artworksDao.getManuallyIdentifiedArtworkIds()).map { it.toDomain() }
    }

    override suspend fun getMovie(artworkId: Long): Movie? {
        return mediasDao.getForArtwork(artworkId = artworkId).find { it.type == ContentType.MOVIE }?.toDomain() as? Movie
    }

    override suspend fun getEpisodes(artworkId: Long): List<Episode> {
        return mediasDao.getForArtwork(artworkId = artworkId).mapNotNull { it.toDomain() as? Episode }.sort()
    }

    override suspend fun getNextEpisodeFor(episode: Episode): Episode? {
        return mediasDao.getNextEpisodeFor(
            artworkId = episode.artworkId,
            season = episode.season,
            number = episode.number
        )?.toDomain() as? Episode
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

    override suspend fun getAllImagesPaths(): ImagesPaths {
        val artworks = artworksDao.getArtworksImages()
        val medias = mediasDao.getMediasImages()
        val seasons = seasonsDao.getImages()

        val images = buildList {
            addAll(artworks.filter { it.imagePath.isNotBlank() }.map { it.imagePath })
            addAll(artworks.filter { it.bannerPath.isNotBlank() }.map { it.bannerPath })
            addAll(medias.filter { it.isNotBlank() })
            addAll(seasons.filter { it.isNotBlank() })
        }

        return ImagesPaths(images = images, logos = artworks.map { it.logoPath })
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

        val existingPaths = files.mapTo(HashSet()) { it.path }
        deleteMedias(medias = getMedias().filter { it.file.path !in existingPaths })
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