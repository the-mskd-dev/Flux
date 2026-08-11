package com.mskd.flux.core.network.tmdb.domain.repository

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.domain.model.Translation
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest

interface ApiRepository {

    suspend fun getArtwork(file: UserFile): Artwork

    suspend fun getGenres() : List<Genre>

    suspend fun getMovie(artworkId: Long, file: UserFile): Movie?

    suspend fun getSeasonAndEpisodes(artworkId: Long, season: Int, files: List<UserFile>): Pair<Season, List<Episode>>?

    suspend fun getTranslation(request: TranslationRequest): Translation?

    suspend fun getGenreIds(artwork: Artwork) : List<Int>

}