package com.kaem.flux.configs

import android.util.Log
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.mockk.every
import io.mockk.mockkStatic

class LogConfig : TestListener {

    override suspend fun beforeSpec(spec: Spec) {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
    }

}