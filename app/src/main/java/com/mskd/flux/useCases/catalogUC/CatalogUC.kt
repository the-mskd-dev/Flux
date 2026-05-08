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
import com.mskd.flux.model.tmdb.TMDBArtwork
import com.mskd.flux.model.tmdb.TMDBEpisode
import com.mskd.flux.model.tmdb.TMDBFolder
import com.mskd.flux.model.tmdb.TMDBMovie
import com.mskd.flux.utils.extensions.groupInFolders
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

interface CatalogUC {
    suspend fun syncCatalog() : Catalog

}

class CatalogUCImpl @Inject constructor(
    private val tmdbRepository: TmdbRepository,
    private val databaseRepository: DatabaseRepository,
    private val filesRepository: FilesRepository
) : CatalogUC {

    override suspend fun syncCatalog(): Catalog {

        val allFiles = filesRepository.getFiles()
        val dbFilesNames = databaseRepository.getAllFileNames()

        val newFiles = allFiles.filter { !dbFilesNames.contains(it.name) }

        val folders = newFiles.groupInFolders()

        val artworksFolders = getArtworksFolders(folders = folders)

        val movies = coroutineScope {

            artworksFolders.filter { it.first.type == ContentType.MOVIE }.map { (artwork, files) ->

                async {

                    val tmdbMovie = tmdbRepository.getTmdbMovie(artworkId = artwork.id)
                    tmdbToFluxMovie(tmdbMovie = tmdbMovie, file = files.first())

                }

            }.awaitAll()
        }

        val episodes = coroutineScope {

            artworksFolders.filter { it.first.type == ContentType.SHOW }.flatMap { (artwork, files) ->

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

                            tmdbToFluxEpisode(tmdbEpisode = tmdbEpisode, file = file)

                        } else {
                            null
                        }

                    }

                }.awaitAll().filterNotNull()

            }

        }

        return Catalog()

    }

    private suspend fun getArtworksFolders(folders: List<UserFolder>) : List<Pair<Artwork, List<UserFile>>> {

        return coroutineScope {

            folders.map { folder ->

                async {

                    val tmdbArtwork = tmdbRepository.getTmdbArtwork(file = folder.files.first())

                    val artwork = if (tmdbArtwork == null) {
                        Artwork.UNKNOWN
                    } else {
                        Artwork(tmdbArtwork = tmdbArtwork)
                    }

                    artwork to folder.files

                }

            }.awaitAll()

        }

    }

    private suspend fun getMovies(artworksFolders: List<Pair<Artwork, List<UserFile>>>) : List<Media> {

        return coroutineScope {

            artworksFolders.filter { it.first.type == ContentType.MOVIE }.map { (artwork, files) ->

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

    private suspend fun tmdbToFluxEpisode(
        tmdbEpisode: TMDBEpisode?,
        file: UserFile
    ): Episode {

        if (tmdbEpisode == null) {
            return Episode(file = file)
        }

        return Episode(
            tmdbEpisode = tmdbEpisode,
            file = file,
        )
    }

}