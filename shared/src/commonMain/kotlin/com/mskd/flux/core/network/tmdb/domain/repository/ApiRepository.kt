package com.mskd.flux.core.network.tmdb.domain.repository

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.core.network.tmdb.domain.model.PersonInfo
import com.mskd.flux.core.network.tmdb.domain.model.Translation
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest

interface ApiRepository {

    /** [Artwork.UNKNOWN] when TMDB cannot identify the file. Throws when the lookup fails (network, API, no token). */
    suspend fun getArtwork(file: UserFile): Artwork

    suspend fun getArtworkById(id: Long, type: ContentType): Artwork?

    suspend fun getGenres() : List<Genre>

    suspend fun getMovie(artworkId: Long, file: UserFile): Movie?

    suspend fun getMoviePeople(artworkId: Long): List<Person>

    suspend fun getMovieImages(artworkId: Long): List<GalleryImage>

    suspend fun getShowImages(artworkId: Long): List<GalleryImage>

    suspend fun getShowPeople(artworkId: Long): List<Person>

    suspend fun getPersonInfo(personId: Int): PersonInfo?

    suspend fun getSeasonAndEpisodes(artworkId: Long, season: Int, files: List<UserFile>): Pair<Season, List<Episode>>?

    suspend fun getTranslation(request: TranslationRequest): Translation?

    suspend fun getGenreIds(artwork: Artwork) : List<Int>

}