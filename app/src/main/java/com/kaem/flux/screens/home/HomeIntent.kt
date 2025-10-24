package com.kaem.flux.screens.home

import com.kaem.flux.model.media.ContentType

sealed class HomeIntent {
    data class OnMediaTap(val mediaId: Long): HomeIntent()
    data class OnCategoryTap(val category: ContentType): HomeIntent()
    data class OnSyncTap(val manualSync: Boolean): HomeIntent()
    object OnSearchTap: HomeIntent()
    object OnSettingsTap: HomeIntent()
    object OnHowToTap: HomeIntent()
    object OnPermissionTap: HomeIntent()
}