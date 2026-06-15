package com.mskd.flux.useCases.player

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import com.mskd.flux.data.repository.settings.SettingsRepository
import kotlinx.coroutines.flow.first

class PipIsEnabledUC(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke() : Boolean {

        val pipIsEnabled = settingsRepository.flow.first().pipIsEnabled
        if (!pipIsEnabled) return  false

        val supportsPip = context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        if (!supportsPip) return false

        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                android.os.Process.myUid(),
                context.packageName
            )

        return mode == AppOpsManager.MODE_ALLOWED
    }
}