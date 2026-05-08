package com.mskd.flux.di

import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.useCases.artworkProgress.ArtworkProgressUC
import com.mskd.flux.useCases.artworkProgress.ArtworkProgressUCImpl
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
    fun provideArtworkProgressUC(
        artworkRepository: ArtworkRepository,
        userRepository: UserRepository
    ) : ArtworkProgressUC {
        return ArtworkProgressUCImpl(
            artworkRepository = artworkRepository,
            userRepository = userRepository
        )
    }
    
}