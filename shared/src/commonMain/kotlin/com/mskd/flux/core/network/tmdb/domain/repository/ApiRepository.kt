package com.mskd.flux.core.network.tmdb.domain.repository

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.domain.model.ArtworkCandidate
import com.mskd.flux.core.network.tmdb.domain.model.ArtworkDetails
import com.mskd.flux.core.network.tmdb.domain.model.PersonInfo
import com.mskd.flux.core.network.tmdb.domain.model.Translation
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.features.gallery.domain.model.GalleryImage

interface ApiRepository {

    /** [Artwork.UNKNOWN] when TMDB cannot identify the file. Throws when the lookup fails (network, API, no token). */
    suspend fun getArtwork(file: UserFile): Artwork

    suspend fun getArtworkById(id: Long, type: ContentType): Artwork?

    /** TMDB results for [title], without year filter, in the TMDB order. Throws when the search fails (network, API, no token). */
    suspend fun searchArtworks(title: String, type: ContentType): List<ArtworkCandidate>

    /** Details, translations and credits of the Artwork in one call; null when TMDB has no details (or no token). */
    suspend fun getDetails(artworkId: Long, type: ContentType, file: UserFile): ArtworkDetails?

    /** Path of the Logo chosen among the images of the Artwork, empty when it has none. Throws when the lookup fails (network, API, no token). */
    suspend fun getLogo(artworkId: Long, type: ContentType): String

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