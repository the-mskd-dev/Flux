package com.mskd.flux.core

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.mockups.MediaMockups
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDatabaseRepository : DatabaseRepository {

    override suspend fun deleteMediasInFolder(folder: UserFolder) {}

    override fun flowArtworks(): Flow<List<Artwork>> {
        return MutableStateFlow(MediaMockups.artworks)
    }

    override fun flowArtwork(artworkId: Long): Flow<Artwork?> {
        return MutableStateFlow(MediaMockups.artworks.find { it.id == artworkId })
    }

    override fun flowMedias(artworkId: Long): Flow<List<Media>> {
        return MutableStateFlow(MediaMockups.allMedias.filter { it.artworkId == artworkId })

    }

    override fun flowSeasons(artworkId: Long): Flow<List<Season>> {
        return MutableStateFlow(MediaMockups.seasons.filter { it.artworkId == artworkId })
    }

    override suspend fun getArtwork(artworkId: Long): Artwork? {
        return MediaMockups.artworks.find { it.id == artworkId }
    }

    override suspend fun getArtworks(): List<Artwork> {
        return MediaMockups.artworks
    }

    override suspend fun getMedias(): List<Media> {
        return MediaMockups.allMedias
    }

    override suspend fun getMediasNotInFiles(files: List<UserFile>): List<Media> {
        return MediaMockups.allMedias.filter { e -> !files.contains(e.file) }
    }

    override suspend fun getMovie(artworkId: Long): Movie? {
        return MediaMockups.movies.find { it.artworkId == artworkId }
    }

    override suspend fun getEpisodes(artworkId: Long): List<Episode> {
        return MediaMockups.episodes.filter { it.artworkId == artworkId }
    }

    override suspend fun getEpisodeCount(artworkId: Long): Int {
        return MediaMockups.episodes.count { it.artworkId == artworkId }
    }

    override suspend fun getEpisodeCountBySeason(
        artworkId: Long,
        season: Int
    ): Int {
        return MediaMockups.episodes.filter { it.artworkId == artworkId }.count { it.season == season }
    }

    override suspend fun getSeasons(artworkId: Long): List<Season> {
        return MediaMockups.seasons.filter { it.artworkId == artworkId }
    }

    override suspend fun getSeasons(): List<Season> {
        return MediaMockups.seasons
    }

    override suspend fun getUnknownMedias(): List<Episode> {
        return MediaMockups.unknowns
    }

    override suspend fun getAllImagesPaths(): List<String> {
        return emptyList()
    }

    override suspend fun updateRealPaths(files: List<UserFile>) {}

    override suspend fun saveArtworks(artworks: List<Artwork>, overrideLastModification: Boolean) {}
    override suspend fun saveMedias(medias: List<Media>) {}

    override suspend fun saveSeasons(seasons: List<Season>) {}

    override suspend fun deleteArtworks(artworks: List<Artwork>) {}
    
    override suspend fun deleteMediasNotInFiles(files: List<UserFile>) {}

    override suspend fun deleteAll() {}
}