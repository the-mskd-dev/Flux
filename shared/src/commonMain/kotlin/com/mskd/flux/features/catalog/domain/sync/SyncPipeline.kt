package com.mskd.flux.features.catalog.domain.sync

import com.mskd.flux.features.catalog.domain.sync.step.ScanStep

/**
 * Runs the Étapes de synchro in order. As soon as the [scanStep] is done, every other step declares its estimated
 * work, so that the Progression de synchro covers the whole Synchro.
 */
class SyncPipeline(
    private val scanStep: ScanStep,
    private val steps: List<SyncStep>,
) {

    suspend fun run(context: SyncContext) {

        context.progress.declare(scanStep.units(context))
        scanStep.run(context)

        val enabled = steps.filter { it.isEnabled(context) }
        enabled.forEach { context.progress.declare(it.units(context)) }

        // Completed only now: the progress must not reach the end of the declared work before every step declared its own
        context.progress.complete(scanStep.units(context))

        enabled.forEach { it.run(context) }

        context.progress.finish()

    }

}
