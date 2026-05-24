package com.mskd.flux.screens.customization

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mskd.flux.data.repository.customization.CustomizationRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.useCases.catalog.CatalogUC
import com.mskd.flux.useCases.images.ImagesUC
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CustomizationViewModel @Inject constructor(
    application: Application,
    private val customizationRepository: CustomizationRepository
) : AndroidViewModel(application) {

}