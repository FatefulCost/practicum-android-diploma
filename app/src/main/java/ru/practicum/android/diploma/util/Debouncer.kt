package ru.practicum.android.diploma.util

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Debouncer(
    private val delayMillis: Long = DEFAULT_DELAY_MILLIS
) {
    private var job: Job? = null

    fun call(action: () -> Unit) {
        job?.cancel()
        job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            delay(delayMillis)
            action()
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }

    companion object {
        const val DEFAULT_DELAY_MILLIS = 500L
    }
}
