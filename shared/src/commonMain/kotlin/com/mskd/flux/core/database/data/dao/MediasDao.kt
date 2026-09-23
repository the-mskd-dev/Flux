package com.mskd.flux.core.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.mskd.flux.core.database.data.model.MediaEntity
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.files.UserFile
import kotlinx.coroutines.flow.Flow

@Dao
interface MediasDao {

    //region Insert

    @Insert
    suspend fun insert(medias: List<MediaEntity>)

    @Upsert
    suspend fun upsert(medias: List<MediaEntity>)

    @Transaction
    suspend fun insertOrUpdate(medias: List<MediaEntity>) {
        if (medias.isEmpty()) return

        // The primary key is (id, artworkId): never save twice the same media identity
        val incoming = medias.distinctBy { it.id to it.artworkId }

        // The path column has a unique index: free the paths owned by another media identity
        val existingByPath = findByPaths(paths = incoming.map { it.path }).associateBy { it.path }
        val stalePaths = incoming
            .filter { media ->
                val existing = existingByPath[media.path] ?: return@filter false
                existing.id != media.id || existing.artworkId != media.artworkId
            }
            .map { it.path }

        if (stalePaths.isNotEmpty()) deleteByPaths(paths = stalePaths)

        upsert(medias = incoming)
    }

    //endregion

    //region Update

    @Update
    suspend fun update(medias: List<MediaEntity>)

    @Transaction
    suspend fun updateRealPaths(files: List<UserFile>) {
        files.forEach { file ->
            updateRealPathIfEmpty(path = file.path, realPath = file.realPath)
        }
    }

    @Query("UPDATE medias SET realPath = :realPath WHERE path = :path AND realPath = ''")
    suspend fun updateRealPathIfEmpty(path: String, realPath: String)

    //endregion

    //region Flow

    @Query("SELECT * FROM medias WHERE artworkId = :artworkId")
    fun flow(artworkId: Long) : Flow<List<MediaEntity>>

    //endregion

    //region Select

    @Query("SELECT * FROM medias WHERE artworkId = :artworkId")
    suspend fun getForArtwork(artworkId: Long) : List<MediaEntity>

    @Query("SELECT * FROM medias")
    suspend fun getAll() : List<MediaEntity>

    @Query("SELECT * FROM medias WHERE name NOT IN (:fileNames)")
    suspend fun getMediasNotInFiles(fileNames: List<String>) : List<MediaEntity>

    @Query("SELECT * FROM medias WHERE artworkId = ${Artwork.UNKNOWN_ID}")
    suspend fun getUnknownMedias() : List<MediaEntity>

    @Query("SELECT * FROM medias WHERE path IN (:paths)")
    suspend fun findByPaths(paths: List<String>): List<MediaEntity>


    @Query("SELECT imagePath FROM medias WHERE imagePath IS NOT NULL")
    suspend fun getMediasImages() : List<String>

    //endregion

    //region Count

    @Query("SELECT COUNT(*) FROM medias WHERE artworkId = :artworkId AND type = :type")
    suspend fun getEpisodeCountByArtworkId(artworkId: Long, type: ContentType = ContentType.SHOW): Int

    @Query("SELECT COUNT(*) FROM medias WHERE artworkId = :artworkId AND season = :season AND type = :type")
    suspend fun getEpisodeCountBySeason(artworkId: Long, season: Int, type: ContentType = ContentType.SHOW): Int

    //endregion

    //region Delete

    @Query("DELETE FROM medias WHERE path IN (:paths)")
    suspend fun deleteByPaths(paths: List<String>)

    @Query("DELETE FROM medias WHERE artworkId IN (:artworkIds)")
    suspend fun deleteMediasByArtworkIds(artworkIds: List<Long>)

    @Query("DELETE FROM medias WHERE id IN (:ids) AND type = :type")
    suspend fun deleteEpisodesByIds(ids: List<Long>, type: ContentType = ContentType.SHOW)

    @Query("DELETE FROM medias WHERE path LIKE :folderPath || '%' ESCAPE '\\'")
    suspend fun deleteMediasInFolder(folderPath: String)

    @Query("DELETE FROM medias")
    suspend fun deleteAllMedias()

    //endregion

}