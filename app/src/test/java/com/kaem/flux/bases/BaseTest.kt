package com.kaem.flux.bases

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

abstract class BaseTest {

    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    open fun setUp() {

        mockkStatic(android.util.Log::class)
        every { Log.i(any(), any()) } returns 0

        Dispatchers.setMain(testDispatcher)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    open fun tearDown() {
        Dispatchers.resetMain()
    }

}