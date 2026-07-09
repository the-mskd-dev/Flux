package com.mskd.flux.features.player.data.usecase

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import com.mskd.flux.features.player.data.PipIsEnabledUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import kotlinx.coroutines.flow.first

class AndroidPipIsEnabledUseCase(
    private val context: Context,
    private val settingsDataStore: SettingsDataStore
): PipIsEnabledUseCase {

    override suspend operator fun invoke() : Boolean {

        val pipIsEnabled = settingsDataStore.flow.first().pipIsEnabled
        if (!pipIsEnabled) return  false

        val supportsPip = context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        if (!supportsPip) return false

        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
            Process.myUid(),
            context.packageName
        )

        return mode == AppOpsManager.MODE_ALLOWED
    }
}