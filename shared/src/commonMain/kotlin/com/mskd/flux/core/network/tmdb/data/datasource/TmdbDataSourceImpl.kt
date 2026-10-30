package com.mskd.flux.core.network.tmdb.data.datasource

import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.model.language.Language
import com.mskd.flux.core.model.language.languageCode
import com.mskd.flux.core.model.language.toTmdbFormat
import com.mskd.flux.core.network.tmdb.data.dto.ArtworkDto
import com.mskd.flux.core.network.tmdb.data.dto.MediaTypeDto
import com.mskd.flux.core.network.tmdb.data.dto.TranslationsDto
import com.mskd.flux.core.network.tmdb.data.dto.genre.GenreDto
import com.mskd.flux.core.network.tmdb.data.dto.images.ImagesDto
import com.mskd.flux.core.network.tmdb.data.dto.movie.MovieDto
import com.mskd.flux.core.network.tmdb.data.dto.people.PeopleDto
import com.mskd.flux.core.network.tmdb.data.dto.people.PersonInfoDto
import com.mskd.flux.core.network.tmdb.data.dto.show.EpisodeDto
import com.mskd.flux.core.network.tmdb.data.dto.show.SeasonDto
import com.mskd.flux.core.network.tmdb.data.dto.show.ShowDto
import com.mskd.flux.core.network.tmdb.data.service.TMDBService
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.report.Trace
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TmdbDataSourceImpl(
    private val tmdbService: TMDBService,
    private val settings: SettingsDataStore,
    private val tokenDataStore: TokenDataStore
) : TmdbDataSource {

    private companion object {
        const val TAG = "TmdbRepositoryImpl"
    }

    private suspend fun tmdbLanguage(): String = settings.getDataLanguage()

    override suspend fun getArtwork(
        file: UserFile
    ): ArtworkDto? {

        // A missing token or a failing request is a failure, not a file that TMDB cannot identify
        check(tokenDataStore.tokenIsAvailable()) { "No TMDB token available" }

        val language = tmdbLanguage()

        return try {

            val searchResults = if (file.isEpisode) {
                tmdbService.searchShow(
                    title = file.nameProperties.title,
                    year = file.nameProperties.year,
                    language = language.toTmdbFormat()
                )
            } else {
                tmdbService.searchMovie(
                    title = file.nameProperties.title,
                    year = file.nameProperties.year,
                    language = language.toTmdbFormat()
                )
            }

            val tmdbArtwork = searchResults.artworkFor(fileName = file.nameProperties.title)?.also {
                it.type = if (file.isEpisode) MediaTypeDto.SHOW else MediaTypeDto.MOVIE
            }

            tmdbArtwork

        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getTmdbArtwork - Fail to get TMDBArtwork for file:${file.name} (${language.toTmdbFormat()})", throwable = e)
            throw e
        }

    }

    override suspend fun searchArtworks(title: String, type: ContentType): List<ArtworkDto> {

        check(tokenDataStore.tokenIsAvailable()) { "No TMDB token available" }

        val language = tmdbLanguage().toTmdbFormat()

        return try {
            when (type) {
                ContentType.MOVIE -> tmdbService.searchMovie(title = title, language = language)
                ContentType.SHOW -> tmdbService.searchShow(title = title, language = language)
            }.results.onEach { it.type = if (type == ContentType.SHOW) MediaTypeDto.SHOW else MediaTypeDto.MOVIE }
        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "searchArtworks - Fail to search $type '$title' ($language)", throwable = e)
            throw e
        }

    }

    override suspend fun getGenres(): List<GenreDto> {

        if (!tokenDataStore.tokenIsAvailable())
            return emptyList()

        val language = tmdbLanguage()

        return coroutineScope {

            val movieGenresDeferred = async {
                runCatching { tmdbService.getMovieGenres(language = language.toTmdbFormat()).genres }
                    .onFailure { Trace.error(TAG, "getGenres for movies failed", it) }
                    .getOrElse { emptyList() }
            }
            val showGenresDeferred = async {
                runCatching { tmdbService.getShowGenres(language = language.toTmdbFormat()).genres }
                    .onFailure { Trace.error(TAG, "getGenres for movies failed", it) }
                    .getOrElse { emptyList() }
            }

            movieGenresDeferred.await() + showGenresDeferred.await()
        }

    }

    override suspend fun getMovie(
        artworkId: Long
    ): MovieDto? {

        if (!tokenDataStore.tokenIsAvailable())
            return null

        val language = tmdbLanguage()

        return try {

            val tmdbMovie = tmdbService.getMovieDetails(
                id = artworkId,
                language = language.toTmdbFormat()
            )

            val translation = tmdbMovie.translations
                ?.takeIf { tmdbMovie.description.isBlank() || tmdbMovie.title.isBlank() }
                ?.translationFor(language)

            if (translation != null) {
                tmdbMovie.copy(
                    title = translation.data.text?.takeIf { it.isNotBlank() } ?: tmdbMovie.title,
                    description = translation.data.overview?.takeIf { it.isNotBlank() } ?: tmdbMovie.description
                )
            } else {
                tmdbMovie
            }

        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getTmdbMovie - Fail to get TMDBMovie for artworkId:$artworkId (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    override suspend fun getMovieImages(artworkId: Long): ImagesDto {
        return tmdbService.getMovieImages(
            artworkId = artworkId,
            language = tmdbLanguage().toTmdbFormat()
        ).getOrThrow()
    }

    override suspend fun getMoviePeople(artworkId: Long): PeopleDto? {

        if (!tokenDataStore.tokenIsAvailable())
            return null

        val language = tmdbLanguage()

        return try {
            tmdbService.getMoviePeople(
                artworkId = artworkId,
                language = language.toTmdbFormat()
            )
        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getMoviePeople - Fail to get people for artworkId:$artworkId (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    override suspend fun getShow(
        artworkId: Long
    ): ShowDto? {

        if (!tokenDataStore.tokenIsAvailable())
            return null

        val language = tmdbLanguage()

        return try {

            val tmdbShow = tmdbService.getShowDetails(
                artworkId = artworkId,
                language = language.toTmdbFormat()
            )

            val translation = tmdbShow.translations
                ?.takeIf { tmdbShow.description.isBlank() || tmdbShow.title.isBlank() }
                ?.translationFor(language)

            if (translation != null) {
                tmdbShow.copy(
                    title = translation.data.text?.takeIf { it.isNotBlank() } ?: tmdbShow.title,
                    description = translation.data.overview?.takeIf { it.isNotBlank() } ?: tmdbShow.description
                )
            } else {
                tmdbShow
            }

        } catch (e: Exception) {
            Trace.error(tag = TAG, message = "getTmdbMovie - Fail to get TMDBShow for artworkId:$artworkId (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    override suspend fun getShowImages(artworkId: Long): ImagesDto {
        return tmdbService.getShowImages(
            artworkId = artworkId,
            language = tmdbLanguage().toTmdbFormat()
        ).getOrThrow()
    }

    override suspend fun getShowPeople(artworkId: Long): PeopleDto? {

        if (!tokenDataStore.tokenIsAvailable())
            return null

        val language = tmdbLanguage()

        return try {
            tmdbService.getShowPeople(
                artworkId = artworkId,
                language = language.toTmdbFormat()
            )
        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getShowPeople - Fail to get people for artworkId:$artworkId (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    override suspend fun getPersonInfo(personId: Int): PersonInfoDto? {

        if (!tokenDataStore.tokenIsAvailable())
            return null

        val language = tmdbLanguage()

        return try {

            tmdbService.getPersonInfo(
                id = personId,
                language = language.toTmdbFormat()
            )

        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getPersonDetails - Fail to get person details for personId:$personId (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    override suspend fun getEpisode(
        artworkId: Long,
        season: Int,
        number: Int
    ): EpisodeDto? {

        if (!tokenDataStore.tokenIsAvailable())
            return null

        val language = tmdbLanguage()

        return try {

            var tmdbEpisode = tmdbService.getEpisode(
                id = artworkId,
                season = season,
                number = number,
                language = language.toTmdbFormat()
            )

            if (tmdbEpisode.description.isBlank() || tmdbEpisode.title.isBlank()) {

                getTranslation(
                    request = TranslationRequest.Episode(
                        artworkId = artworkId,
                        season = season,
                        number = number,
                        language = language
                    ),
                )?.let {
                    tmdbEpisode = tmdbEpisode.copy(
                        title = it.data.name ?: tmdbEpisode.title,
                        description = it.data.overview ?: tmdbEpisode.description
                    )
                }

            }

            tmdbEpisode

        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getTmdbEpisode - Fail to get TMDBEpisode for artworkId:$artworkId, season:$season, number:$number (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    override suspend fun getSeason(artworkId: Long, season: Int): SeasonDto? {

        if (!tokenDataStore.tokenIsAvailable())
            return null

        val language = tmdbLanguage()

        return try {

            coroutineScope {

                val english = Language.ENGLISH.toString()

                // English only completes the blanks of the user language: its failure is not the season's
                val englishSeason = language.takeUnless { it.languageCode() == Language.ENGLISH.code }?.let {
                    async {
                        try {
                            tmdbService.getSeason(id = artworkId, season = season, language = english)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Trace.error(TAG, "getTmdbSeason - Fail to get the English TMDBSeason for artworkId:$artworkId, season:$season", e)
                            null
                        }
                    }
                }

                val tmdbSeason = tmdbService.getSeason(
                    id = artworkId,
                    season = season,
                    language = language.toTmdbFormat()
                )

                englishSeason?.await()?.let { tmdbSeason.completedBy(it) } ?: tmdbSeason

            }

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getTmdbSeason - Fail to get TMDBSeason for artworkId:$artworkId, season:$season (${language.toTmdbFormat()})", throwable = e)
            null
        }

    }

    /** Each blank title and description of the season and of its episodes completed by [fallback]. */
    private fun SeasonDto.completedBy(fallback: SeasonDto): SeasonDto = copy(
        title = title.ifBlank { fallback.title },
        description = description.ifBlank { fallback.description },
        episodes = episodes.map { episode ->
            val fallbackEpisode = fallback.episodes.find { it.number == episode.number }
            episode.copy(
                title = episode.title.ifBlank { fallbackEpisode?.title.orEmpty() },
                description = episode.description.ifBlank { fallbackEpisode?.description.orEmpty() }
            )
        }
    )

    override suspend fun getTranslation(request: TranslationRequest): TranslationsDto.Translation? {

        if (!tokenDataStore.tokenIsAvailable())
            return null

        return try {

            val result = when (request) {
                is TranslationRequest.Movie -> tmdbService.getMovieTranslations(artworkId = request.artworkId)
                is TranslationRequest.Show -> tmdbService.getShowTranslations(artworkId = request.artworkId)
                is TranslationRequest.Season -> tmdbService.getSeasonTranslations(artworkId = request.artworkId, season = request.season)
                is TranslationRequest.Episode -> tmdbService.getEpisodeTranslations(artworkId = request.artworkId, season = request.season, number = request.number)
            }

            val translation = result.translationFor(request.language)

            return translation

        } catch (e: Exception) {
            Napier.e(tag = TAG, message = "getTmdbTranslations - Fail to get translations for $request", throwable = e)
            null
        }

    }

    /** Translation in [language], each blank field completed by English; English when there is none in [language]. */
    private fun TranslationsDto.translationFor(language: String): TranslationsDto.Translation? {

        val requestedTranslation = translations.find { it.language.equals(language.languageCode(), true) && !it.data.overview.isNullOrBlank() }
        val fallbackTranslation = translations.find { it.language.equals(Language.ENGLISH.code, true) && !it.data.overview.isNullOrBlank() }

        return when {
            requestedTranslation != null -> requestedTranslation.copy(
                data = TranslationsDto.Data(
                    name = requestedTranslation.data.text?.ifBlank { fallbackTranslation?.data?.text } ?: "",
                    overview = requestedTranslation.data.overview?.ifBlank { fallbackTranslation?.data?.overview } ?: ""
                )
            )
            else -> fallbackTranslation
        }

    }

}
