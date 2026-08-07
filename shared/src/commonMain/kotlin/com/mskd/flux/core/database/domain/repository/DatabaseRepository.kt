package com.mskd.flux.core.database.domain.repository

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.sources.domain.model.UserFolder
import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {

    // Flows
    fun flowArtworks() : Flow<List<Artwork>>
    fun flowArtwork(artworkId: Long) : Flow<Artwork?>
    fun flowMedias(artworkId: Long): Flow<List<Media>>
    fun flowSeasons(artworkId: Long) : Flow<List<Season>>

    // Save
    suspend fun saveArtworks(artworks: List<Artwork>, overrideLastModification: Boolean = true)
    suspend fun saveMedias(medias: List<Media>)
    suspend fun saveSeasons(seasons: List<Season>)

    // Artworks
    suspend fun getArtwork(artworkId: Long) : Artwork?
    suspend fun getArtworks() : List<Artwork>

    // Medias
    suspend fun getMedias() : List<Media>
    suspend fun getMediasNotInFiles(files: List<UserFile>) : List<Media>

    // Movies
    suspend fun getMovie(artworkId: Long) : Movie?

    // Episodes
    suspend fun getEpisodes(artworkId: Long) : List<Episode>
    suspend fun getEpisodeCount(artworkId: Long): Int
    suspend fun getEpisodeCountBySeason(artworkId: Long, season: Int): Int

    // Seasons
    suspend fun getSeasons(artworkId: Long) : List<Season>
    suspend fun getSeasons() : List<Season>

    // Unknowns
    suspend fun getUnknownMedias() : List<Episode>

    // Images
    suspend fun getAllImagesPaths() : List<String>

    // Update
    suspend fun updateRealPaths(files: List<UserFile>)

    // Delete
    suspend fun deleteArtworks(artworks: List<Artwork>)
    suspend fun deleteMediasNotInFiles(files: List<UserFile>)
    suspend fun deleteMediasInFolder(folder: UserFolder)
    suspend fun deleteAll()

}