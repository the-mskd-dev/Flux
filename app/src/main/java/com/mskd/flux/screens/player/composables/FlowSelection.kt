package com.mskd.flux.screens.player.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Collects only one part of a [StateFlow]: the caller recomposes only when the selected value changes.
 * [select] must be a pure function, it is captured once.
 */
@Composable
fun <T, R> StateFlow<T>.collectSelectedAsState(select: (T) -> R): State<R> {
    val selected = remember(this) { map(select).distinctUntilChanged() }
    return selected.collectAsStateWithLifecycle(initialValue = remember(this) { select(value) })
}
