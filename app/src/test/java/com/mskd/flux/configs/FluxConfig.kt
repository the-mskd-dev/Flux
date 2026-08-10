package com.mskd.flux.configs

import io.kotest.core.spec.Extendable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
fun Extendable.fluxExtensions(
    testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestDispatcher {
    extension(DispatcherConfig(testDispatcher = testDispatcher))
    //extension(LogConfig())

    return testDispatcher
}