package com.mskd.flux.features.catalog.domain.sync

import com.mskd.flux.features.catalog.domain.sync.step.ScanStep

/**
 * Runs the enabled Sync steps in order. When [scanStep] runs, it finishes before every remaining Sync step
 * declares its estimated work, so that Sync progress covers the whole Sync.
 */
class SyncPipeline(
    private val scanStep: ScanStep,
    private val steps: List<SyncStep>,
) {

    suspend fun run(context: SyncContext) {

        val scanUnits = if (scanStep.isEnabled(context)) {
            scanStep.units(context).also { units ->
                context.progress.declare(units)
                scanStep.run(context)
            }
        } else {
            0
        }

        val enabled = steps.filter { it.isEnabled(context) }
        enabled.forEach { context.progress.declare(it.units(context)) }

        // Completed only now: Sync progress must not reach the end of the declared work before every Sync step declared its own
        if (scanUnits > 0) context.progress.complete(scanUnits)

        enabled.forEach { it.run(context) }

        context.progress.finish()

    }

}
