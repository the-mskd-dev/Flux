package com.mskd.flux.core.network.tmdb.data.service

import com.mskd.flux.core.network.tmdb.data.dto.AuthenticationDto
import com.mskd.flux.core.network.tmdb.data.dto.SearchResultsDto
import com.mskd.flux.core.network.tmdb.data.dto.TranslationsDto
import com.mskd.flux.core.network.tmdb.data.dto.genre.GenresResultDto
import com.mskd.flux.core.network.tmdb.data.dto.images.ImagesDto
import com.mskd.flux.core.network.tmdb.data.dto.movie.MovieDto
import com.mskd.flux.core.network.tmdb.data.dto.people.PeopleDto
import com.mskd.flux.core.network.tmdb.data.dto.people.PersonInfoDto
import com.mskd.flux.core.network.tmdb.data.dto.show.EpisodeDto
import com.mskd.flux.core.network.tmdb.data.dto.show.SeasonDto
import com.mskd.flux.core.network.tmdb.data.dto.show.ShowDto

interface TMDBService {

    //region Auth

    suspend fun authenticate() : AuthenticationDto

    //endregion

    //region Movie

    suspend fun searchMovie(
        title: String,
        year: Int? = null,
        language: String
    ) : SearchResultsDto

    suspend fun getMovieDetails(
        id: Long,
        language: String
    ) : MovieDto

    suspend fun getMovieTranslations(
        artworkId: Long,
    ) : TranslationsDto

    suspend fun getMovieGenres(language: String) : GenresResultDto

    suspend fun getMoviePeople(
        artworkId: Long,
        language: String
    ) : PeopleDto

    suspend fun getMovieImages(
        artworkId: Long,
        language: String
    ) : Result<ImagesDto>

    //endregion

    //region Show

    suspend fun searchShow(
        title: String,
        year: Int? = null,
        language: String
    ) : SearchResultsDto

    suspend fun getShowDetails(
        artworkId: Long,
        language: String
    ) : ShowDto

    suspend fun getShowTranslations(
        artworkId: Long,
    ) : TranslationsDto

    suspend fun getShowGenres(language: String) : GenresResultDto

    suspend fun getShowPeople(
        artworkId: Long,
        language: String
    ) : PeopleDto

    //endregion

    //region Episode

    suspend fun getEpisode(
        id: Long,
        season: Int,
        number: Int,
        language: String
    ) : EpisodeDto

    suspend fun getEpisodeTranslations(
        artworkId: Long,
        season: Int,
        number: Int,
    ) : TranslationsDto

    //endregion

    //region Season

    suspend fun getSeason(
        id: Long,
        season: Int,
        language: String
    ) : SeasonDto

    suspend fun getSeasonTranslations(
        artworkId: Long,
        season: Int,
    ) : TranslationsDto

    //endregion

    //region Person

    suspend fun getPersonInfo(
        id: Int,
        language: String
    ) : PersonInfoDto

    //endregion

}