package com.mskd.flux.di

import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.useCases.mediaProgress.MediaProgressUC
import com.mskd.flux.useCases.mediaProgress.MediaProgressUCImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCasesModule {

    @Provides
    @Singleton
    fun provideMediaProgressUC(
        artworkRepository: ArtworkRepository,
        userRepository: UserRepository
    ) : MediaProgressUC {
        return MediaProgressUCImpl(
            artworkRepository = artworkRepository,
            userRepository = userRepository
        )
    }
}