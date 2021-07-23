package io.nacular.doodle.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public object Dispatchers {
    /** Dispatcher that corresponds to the UI thread. */
    public val UI: CoroutineDispatcher = Dispatchers.Default
}