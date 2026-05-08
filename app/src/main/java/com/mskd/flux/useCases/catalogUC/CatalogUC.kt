package com.mskd.flux.useCases.catalogUC

import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.model.Catalog
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.UserFolder
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.utils.extensions.groupInFolders
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface CatalogUC {

    suspend fun flowCatalog() : Flow<List<Artwork>>

    suspend fun syncCatalog() : Catalog

}

class CatalogUCImpl(
    private val tmdbRepository: TmdbRepository,
    private val databaseRepository: DatabaseRepository,
    private val filesRepository: FilesRepository
) : CatalogUC {

    private data class ArtworkFolder(
        val artwork: Artwork,
        val files: List<UserFile>
    )

    override suspend fun flowCatalog(): Flow<List<Artwork>> {
        return databaseRepository.flowArtworks()
    }

    override suspend fun syncCatalog(): Catalog {

        // Get files
        val allFiles = filesRepository.getFiles()
        val dbFilesNames = databaseRepository.getAllFileNames()
        val newFiles = allFiles.filter { !dbFilesNames.contains(it.name) }
        val folders = newFiles.groupInFolders()

        // Get data
        val artworksFolders = getArtworksFolders(folders = folders)
        val movies = getMovies(artworkFolders = artworksFolders)
        val episodes = getEpisodes(artworkFolders = artworksFolders)

        // Save data
        databaseRepository.saveArtworks(artworks = artworksFolders.map { it.artwork })
        databaseRepository.saveMovies(movies = movies.filterIsInstance<Movie>())
        databaseRepository.saveEpisodes(episodes = movies.filterIsInstance<Episode>())
        databaseRepository.saveEpisodes(episodes = episodes)

        return Catalog()

    }

    private suspend fun getArtworksFolders(folders: List<UserFolder>) : List<ArtworkFolder> {

        return coroutineScope {

            folders.map { folder ->

                async {

                    val tmdbArtwork = tmdbRepository.getTmdbArtwork(file = folder.files.first())

                    val artwork = if (tmdbArtwork == null) {
                        Artwork.UNKNOWN
                    } else {
                        Artwork(tmdbArtwork = tmdbArtwork)
                    }

                    ArtworkFolder(
                        artwork = artwork,
                        files = folder.files
                    )

                }

            }.awaitAll()

        }

    }

    private suspend fun getMovies(artworkFolders: List<ArtworkFolder>) : List<Media> {

        return coroutineScope {

            artworkFolders.filter { it.artwork.type == ContentType.MOVIE }.map { (artwork, files) ->

                async {

                    val tmdbMovie = tmdbRepository.getTmdbMovie(artworkId = artwork.id)

                    if (tmdbMovie == null) {
                        Episode(file = files.first())
                    } else {
                        Movie(tmdbMovie = tmdbMovie, file = files.first())
                    }

                }

            }.awaitAll()

        }

    }

    private suspend fun getEpisodes(artworkFolders: List<ArtworkFolder>) : List<Episode> {

        return coroutineScope {

            artworkFolders.filter { it.artwork.type == ContentType.SHOW }.flatMap { (artwork, files) ->

                files.map { file ->

                    val season = file.nameProperties.season
                    val number = file.nameProperties.episode

                    async {

                        if (season != null && number != null) {

                            val tmdbEpisode = tmdbRepository.getTmdbEpisode(
                                artworkId = artwork.id,
                                season = season,
                                number = number
                            )

                            if (tmdbEpisode == null) {
                                Episode(file = file)
                            } else {
                                Episode(
                                    tmdbEpisode = tmdbEpisode,
                                    file = file,
                                )
                            }

                        } else {
                            null
                        }

                    }

                }.awaitAll().filterNotNull()

            }

        }


    }

}