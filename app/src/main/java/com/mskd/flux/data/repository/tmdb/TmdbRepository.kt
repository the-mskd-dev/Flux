package com.mskd.flux.data.repository.tmdb

import com.mskd.flux.model.UserFile
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.tmdb.TMDBArtwork

interface TmdbRepository {

    suspend fun searchTmdbArtworks(file: UserFile) : List<TMDBArtwork>

    suspend fun applyTmdbArtwork(artwork: Artwork, tmdbArtwork: TMDBArtwork)

}