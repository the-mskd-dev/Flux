package com.mskd.flux.screens.unknown

import androidx.lifecycle.ViewModel
import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.user.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UnknownViewModel @Inject constructor(
    private val repository: ArtworkRepository,
    private val userRepository: UserRepository
) : ViewModel() {
}