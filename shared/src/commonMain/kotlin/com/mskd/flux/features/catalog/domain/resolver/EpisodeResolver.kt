package com.mskd.flux.features.catalog.domain.resolver

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.network.tmdb.data.dto.EpisodeDto
import com.mskd.flux.core.network.tmdb.domain.repository.ArtworkRemoteRepository
import com.mskd.flux.features.catalog.domain.model.ArtworkFiles
import com.mskd.flux.features.files.domain.usecase.GetFileDurationUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

interface EpisodeResolver {
    suspend fun resolve(
        artworkFiles: List<ArtworkFiles>,
        episodesDto: List<EpisodeDto>,
        onProgress: () -> Unit
    ): List<Episode>
}

class EpisodeResolverImpl(
    private val remoteRepository: ArtworkRemoteRepository,
    private val settings: SettingsDataStore,
    private val getFileDurationUseCase: GetFileDurationUseCase,
    private val dispatcher: CoroutineDispatcher
) : EpisodeResolver {

    private companion object { const val TAG = "EpisodeResolver" }

    override suspend fun resolve(
        artworkFiles: List<ArtworkFiles>,
        episodesDto: List<EpisodeDto>,
        onProgress: () -> Unit
    ): List<Episode> {

        val language = settings.getDataLanguage()
        val tmdbEpisodesMap = episodesDto.associateBy { Triple(it.artworkId, it.season, it.number) }

        val episodes = supervisorScope {

            artworkFiles.filter { it.artwork.type == ContentType.SHOW }.flatMap { (artwork, files) ->

                files.map { file ->

                    async(dispatcher) {

                        try {

                            val season = file.nameProperties.season
                            val number = file.nameProperties.episode

                            when {
                                artwork.id == Artwork.UNKNOWN_ID ->
                                    Episode(file = file, duration = getFileDurationUseCase(file = file))

                                season != null && number != null -> {

                                    val tmdbEpisode = tmdbEpisodesMap[Triple(artwork.id, season, number)]

                                    if (tmdbEpisode == null) {
                                        Episode(file = file, duration = getFileDurationUseCase(file = file))
                                    } else {

                                        remoteRepository.resolveEpisode(
                                            artworkId = artwork.id,
                                            episodeDto = tmdbEpisode,
                                            file = file,
                                            language = language,
                                            fallbackDuration = { getFileDurationUseCase(file = file) }
                                        )

                                    }

                                }

                                else -> null
                            }

                        } catch (e: Exception) {
                            Trace.error(TAG, "Fail to get episode from ${file.name}", e)
                            null
                        } finally {
                            onProgress()
                        }

                    }

                }.awaitAll().filterNotNull()

            }

        }

        Trace.info(TAG, "Found ${episodes.size} episode(s)")

        return episodes

    }

}