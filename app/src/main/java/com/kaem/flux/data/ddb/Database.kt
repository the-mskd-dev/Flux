package com.kaem.flux.data.ddb

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.kaem.flux.model.flux.FluxArtwork
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.flux.FluxEpisode

@Entity
data class ArtworkEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo val content: String
)

@Entity
data class EpisodeEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo val showId: Int,
    @ColumnInfo val content: String
)

@Dao
interface DatabaseDao {

    @Insert
    suspend fun insertArtworkSummary(artworkSummary: FluxArtworkSummary)

    @Insert
    suspend fun insertEpisode(episode: FluxEpisode)

    @Query("SELECT * FROM artworkEntity")
    suspend fun getAllArtworkSummaries() : List<FluxArtworkSummary>

    @Query("SELECT * FROM episodeentity")
    suspend fun getAllEpisodes() : List<FluxArtworkSummary>

    @Query("SELECT * FROM episodeentity WHERE showId LIKE :showId")
    suspend fun getAllEpisodesFor(showId: Int) : List<FluxEpisode>

    @Delete
    suspend fun deleteArtworkSummary(artworkSummary: FluxArtworkSummary)

    @Delete
    suspend fun deleteEpisode(episode: FluxEpisode)

}