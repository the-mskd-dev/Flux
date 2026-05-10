package com.mskd.flux.data.repository.ddb

import com.mskd.flux.model.UserFile
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {

    // Flows
    fun flowArtworks() : Flow<List<Artwork>>
    fun flowArtwork(artworkId: Long) : Flow<Artwork?>
    fun flowMovie(artworkId: Long) : Flow<Movie?>
    fun flowEpisodes(artworkId: Long) : Flow<List<Episode>>

    // Save
    suspend fun saveArtworks(artworks: List<Artwork>)
    suspend fun saveMovies(movies: List<Movie>)
    suspend fun saveEpisodes(episodes: List<Episode>)

    // Artworks
    suspend fun getArtwork(artworkId: Long) : Artwork?
    suspend fun getArtworks() : List<Artwork>

    // Movies
    suspend fun getMovie(artworkId: Long) : Movie?
    suspend fun getMovies() : List<Movie>
    suspend fun getMoviesNotInFiles(files: List<UserFile>) : List<Movie>

    // Episodes
    suspend fun getEpisode(episodeId: Long) : Episode?
    suspend fun getEpisodes(artworkId: Long) : List<Episode>
    suspend fun getEpisodes() : List<Episode>
    suspend fun getEpisodesNotInFiles(files: List<UserFile>) : List<Episode>
    suspend fun getEpisodeCount(artworkId: Long): Int

    // Unknowns
    suspend fun getUnknownMedias() : List<Episode>

    // Delete
    suspend fun deleteArtworks(artworks: List<Artwork>)
    suspend fun deleteMovies(movies: List<Movie>)
    suspend fun deleteEpisodes(episodes: List<Episode>)
    suspend fun deleteMediasNotInFiles(files: List<UserFile>)

}